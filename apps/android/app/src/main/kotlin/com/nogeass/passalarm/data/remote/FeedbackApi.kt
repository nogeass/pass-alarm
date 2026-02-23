package com.nogeass.passalarm.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object FeedbackApi {
    private const val ENDPOINT = "https://pass-alarm-api.nogeass-inc.workers.dev/feedback"
    private const val API_KEY = "07b7d7c6ad899a7542bbe9a20ad61e5084f93cf6a5ca286144158a3ff0f9986b"

    suspend fun send(
        message: String,
        appVersion: String,
        device: String,
        osVersion: String,
        platform: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ENDPOINT)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("X-API-Key", API_KEY)
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.doOutput = true

            val body = JSONObject().apply {
                put("message", message)
                put("appVersion", appVersion)
                put("device", device)
                put("osVersion", osVersion)
                put("platform", platform)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server error: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
