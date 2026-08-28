package com.wickwirez.myride.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewPromptManager {
    private const val PREFS_NAME = "review_prompt"
    private const val KEY_ACTION_COUNT = "successful_actions"
    private const val KEY_REVIEW_REQUESTED = "review_requested"
    private const val ACTIONS_BEFORE_REVIEW = 3

    fun recordSuccessfulAction(context: Context) {
        val preferences = context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        if (preferences.getBoolean(KEY_REVIEW_REQUESTED, false)) return

        val actionCount = preferences.getInt(KEY_ACTION_COUNT, 0) + 1
        preferences.edit()
            .putInt(KEY_ACTION_COUNT, actionCount)
            .apply()

        if (actionCount < ACTIONS_BEFORE_REVIEW) return

        val activity = context.findActivity() ?: return
        val reviewManager = ReviewManagerFactory.create(activity)

        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                preferences.edit()
                    .putBoolean(KEY_REVIEW_REQUESTED, true)
                    .apply()

                reviewManager.launchReviewFlow(activity, request.result)
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var currentContext: Context = this

        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }

        return currentContext as? Activity
    }
}
