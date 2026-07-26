package com.wickwirez.myride.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// role is "user" or "model" (Gemini's name for the assistant turn)
data class ChatMessage(val role: String, val content: String)

object GeminiApiClient {

    private const val MODEL = "gemini-3.5-flash-lite"

    suspend fun sendMessage(
        apiKey: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
            )
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20000
                readTimeout = 30000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
            }

            val contentsJson = JSONArray()
            history.forEach { msg ->
                contentsJson.put(
                    JSONObject().apply {
                        put("role", msg.role)
                        put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                    }
                )
            }

            val body = JSONObject().apply {
                put(
                    "system_instruction",
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", systemPrompt))
                    )
                )
                put("contents", contentsJson)
                put(
                    "generationConfig",
                    JSONObject().put("maxOutputTokens", 1024)
                )
            }

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode !in 200..299) {
                val message = try {
                    JSONObject(responseText).optJSONObject("error")?.optString("message")
                } catch (e: Exception) {
                    null
                } ?: "Request failed (HTTP $responseCode)"
                return@withContext Result.failure(Exception(message))
            }

            val json = JSONObject(responseText)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No response returned"))
            }

            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")
                ?.optJSONArray("parts")

            val text = parts?.let { arr ->
                (0 until arr.length())
                    .mapNotNull { arr.getJSONObject(it).optString("text").takeIf { t -> t.isNotBlank() } }
                    .joinToString("\n")
            }

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("No response text returned"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
