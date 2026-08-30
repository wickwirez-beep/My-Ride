package com.wickwirez.myride.data

import android.content.Context
import android.media.MediaPlayer
import com.wickwirez.myride.R

/**
 * Plays Wick's voice clips. Only one clip plays at a time — a new request
 * releases whatever is currently playing, so overlapping triggers can't
 * stack up on top of each other.
 */
object MascotVoice {

    private var player: MediaPlayer? = null

    enum class Clip(val resId: Int) {
        GREETING(R.raw.wick_greeting),
        SERVICE_DUE(R.raw.wick_service_due),
        SERVICE_OVERDUE(R.raw.wick_service_overdue),
        FILLUP_LOGGED(R.raw.wick_fillup_logged),
        SERVICE_LOGGED(R.raw.wick_service_logged),
        HEALTHY(R.raw.wick_healthy)
    }

    fun play(context: Context, clip: Clip) {
        if (!MascotPrefs.isEnabled(context)) return
        if (!MascotPrefs.isVoiceEnabled(context)) return

        release()
        try {
            player = MediaPlayer.create(context.applicationContext, clip.resId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            release()
        }
    }

    fun release() {
        try {
            player?.release()
        } catch (e: Exception) {
            // nothing useful to do if release itself fails
        }
        player = null
    }
}

private val announcedVehicles = mutableSetOf<Long>()

fun playStatusOnce(context: Context, vehicleId: Long, clip: MascotVoice.Clip) {
    if (!announcedVehicles.add(vehicleId)) return
    MascotVoice.play(context, clip)
}
