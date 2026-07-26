package com.wickwirez.myride.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val role: String, val content: String)

object ClaudeApiClient {

    suspend fun sendMessage(
        apiKey: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.anthropic.com/v1/messages")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
                setRequestProperty("x-api-key", apiKey)
                setRequestProperty("anthropic-version", "2023-06-01")
                setRequestProperty("content-type", "application/json")
            }

            val messagesJson = JSONArray()
            history.forEach { msg ->
                messagesJson.put(
                    JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    }
                )
            }

            val body = JSONObject().apply {
                put("model", "claude-haiku-4-5-20251001")
                put("max_tokens", 1024)
                put("system", systemPrompt)
                put("messages", messagesJson)
            }

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode !in 200..299) {
                val errorJson = try { JSONObject(responseText) } catch (e: Exception) { null }
                val message = errorJson?.optJSONObject("error")?.optString("message")
                    ?: "Request failed (HTTP $responseCode)"
                return@withContext Result.failure(Exception(message))
            }

            val json = JSONObject(responseText)
            val contentArray = json.optJSONArray("content")
            val textPart = contentArray?.let { arr ->
                (0 until arr.length())
                    .map { arr.getJSONObject(it) }
                    .firstOrNull { it.optString("type") == "text" }
                    ?.optString("text")
            }

            if (!textPart.isNullOrBlank()) {
                Result.success(textPart)
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
