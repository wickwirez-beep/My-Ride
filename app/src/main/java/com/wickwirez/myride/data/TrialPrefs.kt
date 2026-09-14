package com.wickwirez.myride.data

import android.content.Context
import java.util.concurrent.TimeUnit

object TrialPrefs {
    private const val PREFS_NAME = "myride_trial"
    private const val KEY_FIRST_LAUNCH = "first_launch_time"
    private const val KEY_UNLOCKED = "is_unlocked"

    private val TRIAL_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

    private fun getOrSetFirstLaunchTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getLong(KEY_FIRST_LAUNCH, -1L)
        if (existing != -1L) return existing

        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_FIRST_LAUNCH, now).apply()
        return now
    }

    fun isTrialActive(context: Context): Boolean {
        val firstLaunch = getOrSetFirstLaunchTime(context)
        val elapsed = System.currentTimeMillis() - firstLaunch
        return elapsed < TRIAL_DURATION_MILLIS
    }

    fun daysRemainingInTrial(context: Context): Int {
        val firstLaunch = getOrSetFirstLaunchTime(context)
        val elapsed = System.currentTimeMillis() - firstLaunch
        val remainingMillis = TRIAL_DURATION_MILLIS - elapsed
        if (remainingMillis <= 0) return 0
        return TimeUnit.MILLISECONDS.toDays(remainingMillis).toInt() + 1
    }

    fun isUnlocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_UNLOCKED, false)
    }

    fun setUnlocked(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_UNLOCKED, true).apply()
    }

    fun hasAccess(context: Context): Boolean {
        return isUnlocked(context) || isTrialActive(context)
    }
}
