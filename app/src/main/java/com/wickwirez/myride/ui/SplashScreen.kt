package com.wickwirez.myride.ui

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Native pixel dimensions of splash_bg.png, used to map the needle's
// hub position and the needle sprite's own size onto whatever size
// the background actually renders at on a given device.
private const val POSTER_W = 736f
private const val POSTER_H = 1418f

// Hub (pivot) position in splash_bg.png pixel coordinates. Measured
// directly from the artwork (Hough circle fit on the bezel + pivot cap).
private const val HUB_X = 375f
private const val HUB_Y = 283f

// splash_needle.png is a square sprite centered exactly on the hub.
private const val NEEDLE_SPRITE_SIZE = 420f

// rotationZ value that points the needle at each whole-number mark on the
// dial (index = RPM x1000, e.g. ROT_AT_MARK[4] points at "4" / 4000 RPM).
// Measured directly from splash_bg.png (tick-mark pixel positions) and
// splash_needle.png (pivot-to-tip angle) — not eyeballed — so a sweep
// between any two of these stays on real numbers and never dips into
// the unmarked gap between "8" and "0" at the bottom of the dial.
private val ROT_AT_MARK = floatArrayOf(
    136.0f, 167.97f, 195.82f, 226.01f, 258.73f, 290.99f, 321.17f, 348.28f, 375.5f
)

// Converts a dial value (RPM x1000, e.g. 4.2f = 4200 RPM) to the
// rotationZ needed to point the needle there, linearly interpolating
// between the nearest two measured marks above.
private fun rpmToAngle(rpmThousands: Float): Float {
    val clamped = rpmThousands.coerceIn(0f, 8f)
    val i0 = clamped.toInt().coerceIn(0, 7)
    val i1 = i0 + 1
    val frac = clamped - i0
    return ROT_AT_MARK[i0] + frac * (ROT_AT_MARK[i1] - ROT_AT_MARK[i0])
}

// Idle and throttle-blip peak, in RPM x1000 — matches the "RPM X 1000"
// label on the dial face. Tune PEAK_RPM within the 3.8–4.5 range to taste.
private const val IDLE_RPM = 0.8f
private const val PEAK_RPM = 4.2f

// Fraction of the sound clip's duration spent "at throttle" before
// easing back to idle. Adjust to match where the engine sound's pitch
// actually peaks in truck_sound's waveform.
private const val PEAK_HOLD_FRACTION = 0.4f

// Per-frame smoothing factor (0–1). Higher = needle reacts faster but
// more abruptly; lower = laggier but silkier. 0.18 matches a real
// needle's mechanical inertia without feeling sluggish.
private const val NEEDLE_SMOOTHING = 0.18f

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val needleRotation = remember { Animatable(rpmToAngle(IDLE_RPM)) }
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.truck_sound) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 500))

        val soundDurationMs = if (mediaPlayer != null) {
            mediaPlayer.start()
            mediaPlayer.duration.toLong().coerceIn(0L, 8000L)
        } else {
            3000L
        }
        val peakHoldMs = (soundDurationMs * PEAK_HOLD_FRACTION).toLong()

        // Continuous per-frame follow: the needle chases a target RPM
        // (peak while "on throttle", idle once we lift off) using simple
        // exponential smoothing, so motion is fluid with no discrete
        // jumps or jitter — same shape as a real needle's inertia.
        val needleJob = launch {
            var startFrame = -1L
            while (true) {
                val elapsedMs = withFrameNanos { frameTime ->
                    if (startFrame < 0) startFrame = frameTime
                    (frameTime - startFrame) / 1_000_000L
                }
                if (elapsedMs >= soundDurationMs) break
                val targetRpm = if (elapsedMs < peakHoldMs) PEAK_RPM else IDLE_RPM
                val targetAngle = rpmToAngle(targetRpm)
                needleRotation.snapTo(
                    needleRotation.value + (targetAngle - needleRotation.value) * NEEDLE_SMOOTHING
                )
            }
        }

        delay(soundDurationMs)
        needleJob.cancel()
        needleRotation.animateTo(rpmToAngle(IDLE_RPM), tween(200))

        alpha.animateTo(0f, animationSpec = tween(durationMillis = 400))
        onFinished()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value)
        ) {
            val availW = maxWidth.value
            val availH = maxHeight.value
            val scale = minOf(availW / POSTER_W, availH / POSTER_H)

            val dispW = (POSTER_W * scale).dp
            val dispH = (POSTER_H * scale).dp
            val offsetX = ((availW - POSTER_W * scale) / 2f).dp
            val offsetY = ((availH - POSTER_H * scale) / 2f).dp

            val needleSizeDp = (NEEDLE_SPRITE_SIZE * scale).dp
            val needleLeft = offsetX + ((HUB_X - NEEDLE_SPRITE_SIZE / 2f) * scale).dp
            val needleTop = offsetY + ((HUB_Y - NEEDLE_SPRITE_SIZE / 2f) * scale).dp

            Image(
                painter = painterResource(id = R.drawable.splash_bg),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = offsetX, y = offsetY)
                    .size(dispW, dispH),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = needleLeft, y = needleTop)
                    .size(needleSizeDp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_needle),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = needleRotation.value
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
