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

// Native pixel dimensions of the NEW splash_bg.png (re-extracted from the
// updated poster source, 1000223139.png). Both assets below were rebuilt
// from that same file, so all geometry here is measured in ITS coordinate
// space, not the old 736x1418 asset's.
private const val POSTER_W = 1024f
private const val POSTER_H = 1535f

// Hub (pivot) position in splash_bg.png pixel coordinates. Measured via
// Hough circle fit directly on the bezel ring (center 516.75, 294.75,
// radius 251.2) — precise to sub-pixel, not eyeballed.
private const val HUB_X = 516.75f
private const val HUB_Y = 294.75f

// splash_needle.png is a square sprite centered exactly on the hub.
private const val NEEDLE_SPRITE_SIZE = 420f

// rotationZ value that points the needle at each whole-number mark on the
// dial (index = RPM x1000, e.g. ROT_AT_MARK[4] points at "4" / 4000 RPM).
//
// Built in two parts, both measured from this same source image:
//  1. Each mark's raw angular position was carried over from the earlier
//     precise measurement of the (nearly identical) gauge artwork, since
//     this image's numbers were too low-contrast to re-detect reliably.
//  2. The needle's own native "at rest" angle (rotationZ = 0) was measured
//     fresh from THIS image, since the needle is actually drawn in it —
//     found via a weighted centroid of the needle's red pixels in its
//     clean tip zone: 234.64 degrees.
// Every mark's rotationZ = (raw mark angle) - (needle's native angle), so
// the sprite's real drawn orientation is the zero point.
private val ROT_AT_MARK = floatArrayOf(
    -90.04f, -57.79f, -29.01f, 1.15f, 35.23f, 69.18f, 100.08f, 128.26f, 157.66f
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
// label on the dial face. Tune PEAK_RPM within the 3.8-4.5 range to taste.
private const val IDLE_RPM = 0.8f
private const val PEAK_RPM = 4.2f

// Fraction of the sound clip's duration spent "at throttle" before
// easing back to idle. Still an estimate, not measured from truck_sound's
// actual waveform — tune this once we nail the timing sync separately.
private const val PEAK_HOLD_FRACTION = 0.4f

// Per-frame smoothing factor (0-1). Higher = needle reacts faster but
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
