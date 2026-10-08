package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(
    private var customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.takeIf { it.isNotBlank() }
    }

    fun getApiKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrEmpty()) return custom

        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun streamGenerateContent(
        modelName: String,
        systemInstruction: String?,
        contentsPayload: JSONArray,
        onChunkReceived: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("MISSING_API_KEY")
            )
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsPayload)

            if (!systemInstruction.isNullOrBlank()) {
                val sysContent = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                }
                put("systemInstruction", sysContent)
            }

            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            }
            put("generationConfig", genConfig)
        }

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:streamGenerateContent?alt=sse&key=$key"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val fullResponseBuilder = StringBuilder()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                Log.e("GeminiClient", "API Error ${response.code}: $errorBody")
                return@withContext Result.failure(
                    Exception("API Error ${response.code}: $errorBody")
                )
            }

            val responseBody = response.body
                ?: return@withContext Result.failure(Exception("Empty response body"))

            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue

                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                    try {
                        val json = JSONObject(jsonPayload)
                        val candidates = json.optJSONArray("candidates") ?: continue
                        if (candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    val text = part.optString("text")
                                    if (text.isNotEmpty()) {
                                        fullResponseBuilder.append(text)
                                        onChunkReceived(text)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("GeminiClient", "Error parsing chunk: ${e.message}")
                    }
                }
            }

            val finalResult = fullResponseBuilder.toString()
            if (finalResult.isEmpty()) {
                Result.failure(Exception("No content returned from AI"))
            } else {
                Result.success(finalResult)
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Network execution failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
