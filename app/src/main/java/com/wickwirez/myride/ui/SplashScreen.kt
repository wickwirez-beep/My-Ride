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

// Hub (pivot) position in splash_bg.png pixel coordinates.
private const val HUB_X = 367f
private const val HUB_Y = 285f

// splash_needle.png is a square sprite centered exactly on the hub.
private const val NEEDLE_SPRITE_SIZE = 420f

// rotationZ = 0 shows the needle exactly as drawn in splash_needle.png,
// which is the "hero pose" resting a little past 7.
// Calibrated directly against the "1" and "7" tick marks on the dial
// (not extrapolated), and kept strictly between them so the needle
// never approaches the unmarked zone near 0/8 at the bottom.
private const val ROT_IDLE = 189f       // sits right at "1"
private const val ROT_REV1_PEAK = 11f   // sits right at "7"
private const val ROT_DIP1 = 144f       // ~2.5
private const val ROT_REV2_PEAK = 17f   // ~6.8, just shy of "7"
private const val ROT_DIP2 = 115f       // ~3.5

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val needleRotation = remember { Animatable(ROT_IDLE) }
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
            mediaPlayer.duration.toLong().coerceAtLeast(0L)
        } else {
            3000L
        }

        // Needle sits at idle, then revs in time with the engine sound,
        // ending on the resting hero pose. Runs concurrently with the
        // delay below so it's finished (or nearly so) by the time the
        // sound clip ends.
        launch {
            needleRotation.snapTo(ROT_IDLE)
            val d = soundDurationMs.coerceAtLeast(600L)
            needleRotation.animateTo(ROT_REV1_PEAK, tween((d * 0.18).toInt()))
            needleRotation.animateTo(ROT_DIP1, tween((d * 0.15).toInt()))
            needleRotation.animateTo(ROT_REV2_PEAK, tween((d * 0.18).toInt()))
            needleRotation.animateTo(ROT_DIP2, tween((d * 0.12).toInt()))
            // Settle back down to idle (~700-750 RPM) rather than staying
            // revved — matches a real engine coming off the throttle.
            needleRotation.animateTo(ROT_IDLE, tween((d * 0.30).toInt()))
        }

        delay(soundDurationMs)

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
