package com.wickwirez.myride.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Stores the user's own Gemini API key encrypted at rest on-device.
// The key never leaves the device except in direct HTTPS calls to
// Google's Gemini API that the user's own key authenticates.
object ApiKeyStore {

    private const val PREFS_NAME = "myride_secure_prefs"
    private const val KEY_API_KEY = "gemini_api_key"

    private fun prefs(context: Context) = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getApiKey(context: Context): String? =
        prefs(context).getString(KEY_API_KEY, null)

    fun setApiKey(context: Context, key: String) {
        prefs(context).edit().putString(KEY_API_KEY, key).apply()
    }

    fun clearApiKey(context: Context) {
        prefs(context).edit().remove(KEY_API_KEY).apply()
    }
}
