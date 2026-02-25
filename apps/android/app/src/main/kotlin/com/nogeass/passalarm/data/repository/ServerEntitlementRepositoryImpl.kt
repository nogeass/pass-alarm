package com.nogeass.passalarm.data.repository

import android.content.Context
import com.nogeass.passalarm.data.remote.PassAlarmApi
import com.nogeass.passalarm.domain.model.ProSource
import com.nogeass.passalarm.domain.model.ProTier
import com.nogeass.passalarm.domain.repository.ServerEntitlement
import com.nogeass.passalarm.domain.repository.ServerEntitlementRepository
import com.nogeass.passalarm.domain.service.AuthService
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerEntitlementRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    @ApplicationContext private val context: Context,
) : ServerEntitlementRepository {

    private val prefs = context.getSharedPreferences("server_entitlements", Context.MODE_PRIVATE)
    private val cache = mutableListOf<ServerEntitlement>()

    init {
        // Restore cached entitlements from SharedPreferences on init
        val json = prefs.getString(KEY_ENTITLEMENTS, null)
        if (json != null) {
            runCatching {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    cache.add(arr.getJSONObject(i).toEntitlement())
                }
            }
        }
    }

    override suspend fun fetchEntitlements(): List<ServerEntitlement> {
        val idToken = authService.getIDToken()
        val response = PassAlarmApi.getEntitlements(idToken)
        if (!response.ok) {
            throw Exception(response.error ?: "Failed to fetch entitlements")
        }
        val entitlements = response.entitlements.map { it.toDomain() }
        updateCache(entitlements)
        return entitlements
    }

    override fun cachedEntitlements(): List<ServerEntitlement> = cache.toList()

    override suspend fun claimToken(token: String): ServerEntitlement {
        val idToken = authService.getIDToken()
        val response = PassAlarmApi.claimToken(token, idToken)
        if (!response.ok || response.entitlement == null) {
            throw Exception(response.error ?: "Failed to claim token")
        }
        val entitlement = response.entitlement.toDomain()
        val updated = cache.toMutableList().apply { add(entitlement) }
        updateCache(updated)
        return entitlement
    }

    override suspend fun isRedeemDisabled(): Boolean {
        val response = PassAlarmApi.getConfig()
        return response.redeemDisabled
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateCache(entitlements: List<ServerEntitlement>) {
        cache.clear()
        cache.addAll(entitlements)
        persistCache()
    }

    private fun persistCache() {
        val arr = JSONArray()
        for (e in cache) {
            arr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("tier", e.tier.name)
                    put("source", e.source.name)
                    put("grantedAt", e.grantedAt)
                    if (e.expiresAt != null) put("expiresAt", e.expiresAt)
                    else put("expiresAt", JSONObject.NULL)
                },
            )
        }
        prefs.edit().putString(KEY_ENTITLEMENTS, arr.toString()).apply()
    }

    private fun PassAlarmApi.EntitlementJson.toDomain(): ServerEntitlement =
        ServerEntitlement(
            id = id,
            tier = when (tier.lowercase()) {
                "pro" -> ProTier.PRO
                else -> ProTier.FREE
            },
            source = when (source.lowercase()) {
                "crowdfund" -> ProSource.CROWDFUND
                "manual" -> ProSource.MANUAL
                else -> ProSource.STORE
            },
            grantedAt = parseIso8601(grantedAt),
            expiresAt = expiresAt?.let { parseIso8601(it) },
        )

    private fun parseIso8601(dateStr: String): Long {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            fmt.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            try {
                val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                fmt.timeZone = TimeZone.getTimeZone("UTC")
                fmt.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun JSONObject.toEntitlement(): ServerEntitlement =
        ServerEntitlement(
            id = optInt("id"),
            tier = when (optString("tier", "FREE").uppercase()) {
                "PRO" -> ProTier.PRO
                else -> ProTier.FREE
            },
            source = when (optString("source", "STORE").uppercase()) {
                "CROWDFUND" -> ProSource.CROWDFUND
                "MANUAL" -> ProSource.MANUAL
                else -> ProSource.STORE
            },
            grantedAt = optLong("grantedAt", 0L),
            expiresAt = if (isNull("expiresAt")) null else optLong("expiresAt"),
        )

    companion object {
        private const val KEY_ENTITLEMENTS = "cached_entitlements"
    }
}
