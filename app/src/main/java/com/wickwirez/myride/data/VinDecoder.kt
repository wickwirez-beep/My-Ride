package com.wickwirez.myride.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VinDecodeResult(
    val year: Int?,
    val make: String,
    val model: String,
    val trim: String
)

object VinDecoder {

    suspend fun decode(vin: String): VinDecodeResult? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValues/$vin?format=json")
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(body).optJSONArray("Results") ?: return@withContext null
            if (results.length() == 0) return@withContext null

            val result = results.getJSONObject(0)
            val make = result.optString("Make", "").trim()
            val model = result.optString("Model", "").trim()
            val trim = result.optString("Trim", "").trim()
            val year = result.optString("ModelYear", "").trim().toIntOrNull()

            if (make.isBlank() && model.isBlank()) return@withContext null

            VinDecodeResult(year = year, make = make, model = model, trim = trim)
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
