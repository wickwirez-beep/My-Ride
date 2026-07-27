package com.wickwirez.myride.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class RecallInfo(
    val campaignNumber: String,
    val component: String,
    val summary: String,
    val consequence: String,
    val remedy: String,
    val reportDate: String
)

object RecallChecker {

    suspend fun fetchRecalls(make: String, model: String, modelYear: Int): Result<List<RecallInfo>> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val encodedMake = URLEncoder.encode(make, "UTF-8")
                val encodedModel = URLEncoder.encode(model, "UTF-8")
                val url = URL(
                    "https://api.nhtsa.gov/recalls/recallsByVehicle?make=$encodedMake" +
                        "&model=$encodedModel&modelYear=$modelYear"
                )
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                }

                val responseCode = connection.responseCode

                // NHTSA's gateway has been observed returning a non-200 status code
                // while still sending back a genuinely successful JSON body. So read
                // whichever stream is available and try to parse it as real recall
                // data first — only treat it as a true failure if that parsing fails.
                val body = try {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }

                val parsed = body?.let {
                    try {
                        JSONObject(it)
                    } catch (e: Exception) {
                        null
                    }
                }

                val resultsArray = parsed?.optJSONArray("results")

                if (resultsArray == null) {
                    val message = parsed?.optString("Message")
                    return@withContext Result.failure(
                        Exception(
                            message?.takeIf { it.isNotBlank() }
                                ?: "Request failed (HTTP $responseCode)"
                        )
                    )
                }

                val recalls = (0 until resultsArray.length()).map { i ->
                    val o = resultsArray.getJSONObject(i)
                    RecallInfo(
                        campaignNumber = o.optString("NHTSACampaignNumber", ""),
                        component = o.optString("Component", ""),
                        summary = o.optString("Summary", ""),
                        consequence = o.optString("Consequence", ""),
                        remedy = o.optString("Remedy", ""),
                        reportDate = o.optString("ReportReceivedDate", "")
                    )
                }

                Result.success(recalls)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
}
