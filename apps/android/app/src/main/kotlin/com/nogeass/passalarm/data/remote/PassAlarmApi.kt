package com.nogeass.passalarm.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Crowdfunding redemption API client.
 *
 * Uses [HttpURLConnection] for consistency with [FeedbackApi] – no OkHttp dependency.
 */
object PassAlarmApi {

    private const val BASE_URL = "https://pass-alarm-api.nogeass-inc.workers.dev"

    // ── Response models ──────────────────────────────────────────────────────

    data class EntitlementJson(
        val id: Int,
        val tier: String,
        val source: String,
        val grantedAt: String,
        val expiresAt: String?,
    )

    data class ClaimResponse(
        val ok: Boolean,
        val entitlement: EntitlementJson?,
        val error: String?,
    )

    data class EntitlementsResponse(
        val ok: Boolean,
        val entitlements: List<EntitlementJson>,
        val error: String?,
    )

    data class ConfigResponse(
        val ok: Boolean,
        val redeemDisabled: Boolean,
        val error: String?,
    )

    // ── API methods ──────────────────────────────────────────────────────────

    suspend fun claimToken(token: String, idToken: String): ClaimResponse =
        withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/api/redeem/claim")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $idToken")
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
            }

            val body = JSONObject().apply {
                put("token", token)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val code = conn.responseCode
            val responseText = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (code !in 200..299) {
                val errorJson = runCatching { JSONObject(responseText) }.getOrNull()
                return@withContext ClaimResponse(
                    ok = false,
                    entitlement = null,
                    error = errorJson?.optString("error") ?: "Server error: $code",
                )
            }

            val json = JSONObject(responseText)
            val ent = json.optJSONObject("entitlement")?.let { parseEntitlement(it) }
            ClaimResponse(ok = true, entitlement = ent, error = null)
        }

    suspend fun getEntitlements(idToken: String): EntitlementsResponse =
        withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/api/me/entitlements")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $idToken")
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            val code = conn.responseCode
            val responseText = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (code !in 200..299) {
                val errorJson = runCatching { JSONObject(responseText) }.getOrNull()
                return@withContext EntitlementsResponse(
                    ok = false,
                    entitlements = emptyList(),
                    error = errorJson?.optString("error") ?: "Server error: $code",
                )
            }

            val json = JSONObject(responseText)
            val arr = json.optJSONArray("entitlements") ?: JSONArray()
            val entitlements = (0 until arr.length()).map { parseEntitlement(arr.getJSONObject(it)) }
            EntitlementsResponse(ok = true, entitlements = entitlements, error = null)
        }

    suspend fun getConfig(): ConfigResponse =
        withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/api/config")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            val code = conn.responseCode
            val responseText = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (code !in 200..299) {
                return@withContext ConfigResponse(
                    ok = false,
                    redeemDisabled = false,
                    error = "Server error: $code",
                )
            }

            val json = JSONObject(responseText)
            ConfigResponse(
                ok = true,
                redeemDisabled = json.optString("REDEEM_DISABLED", "false") == "true",
                error = null,
            )
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parseEntitlement(json: JSONObject): EntitlementJson =
        EntitlementJson(
            id = json.optInt("id"),
            tier = json.optString("tier", "pro"),
            source = json.optString("source", "crowdfund"),
            grantedAt = json.optString("granted_at", ""),
            expiresAt = if (json.isNull("expires_at")) null else json.optString("expires_at"),
        )
}
