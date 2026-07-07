package com.example.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class GeminiApiClient(private val apiKey: String) {
    private val client = OkHttpClient()
    private val tag = "GeminiApiClient"

    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
    }

    suspend fun sendRequest(prompt: String, systemInstruction: String = ""): String? {
        if (!isApiKeyConfigured()) {
            Log.e(tag, "Gemini API Key is not configured correctly.")
            return null
        }
        
        // This is structured to connect to Gemini Generative Language REST Endpoint
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val jsonPayload = """
            {
              "contents": [{
                "parts": [{"text": "$prompt"}]
              }],
              "systemInstruction": {
                "parts": [{"text": "$systemInstruction"}]
              }
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string()
                    Log.d(tag, "Response: $rawBody")
                    rawBody
                } else {
                    Log.e(tag, "Unsuccessful response from Gemini: ${response.code}")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(tag, "Network call failed", e)
            null
        }
    }
}
