package com.wickwirez.myride.data

import android.content.Context

object OnboardingPrefs {
    private const val PREFS_NAME = "myride_onboarding"
    private const val KEY_SEEN = "has_seen_onboarding"

    fun hasSeenOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SEEN, false)
    }

    fun setSeenOnboarding(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEN, true).apply()
    }
}
