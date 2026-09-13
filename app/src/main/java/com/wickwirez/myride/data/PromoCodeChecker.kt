package com.wickwirez.myride.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object PromoCodeChecker {

    private const val GIST_RAW_URL =
        "https://gist.githubusercontent.com/wickwirez-beep/c5b2aef1a128e040de432e202914fe3c/raw/promo_codes.txt"

    suspend fun redeem(code: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(GIST_RAW_URL)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }

                val validCodes = body.lines()
                    .map { it.trim().uppercase() }
                    .filter { it.isNotBlank() }

                val isValid = validCodes.contains(code.trim().uppercase())
                Result.success(isValid)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
}
