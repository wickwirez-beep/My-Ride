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

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    val detail = if (!errorBody.isNullOrBlank()) {
                        try {
                            JSONObject(errorBody).optString("Message").ifBlank { errorBody }
                        } catch (e: Exception) {
                            errorBody
                        }
                    } else {
                        null
                    }
                    return@withContext Result.failure(
                        Exception(
                            "Request failed (HTTP ${connection.responseCode})" +
                                if (detail != null) ": $detail" else ""
                        )
                    )
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val results = JSONObject(body).optJSONArray("results") ?: JSONArray()

                val recalls = (0 until results.length()).map { i ->
                    val o = results.getJSONObject(i)
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
