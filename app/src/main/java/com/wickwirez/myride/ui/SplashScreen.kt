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

private const val POSTER_W = 1024f
private const val POSTER_H = 1535f

private const val HUB_X = 516.75f
private const val HUB_Y = 294.75f

private const val NEEDLE_SPRITE_SIZE = 420f

private val ROT_AT_MARK = floatArrayOf(
    -90.04f, -57.79f, -29.01f, 1.15f, 35.23f, 69.18f, 100.08f, 128.26f, 157.66f
)

private fun rpmToAngle(rpmThousands: Float): Float {
    val clamped = rpmThousands.coerceIn(0f, 8f)
    val i0 = clamped.toInt().coerceIn(0, 7)
    val i1 = i0 + 1
    val frac = clamped - i0
    return ROT_AT_MARK[i0] + frac * (ROT_AT_MARK[i1] - ROT_AT_MARK[i0])
}

// Needle sits statically at this RPM (x1000) for the whole splash — no
// sweep/rev animation, just parked here while the sound plays.
private const val TARGET_RPM = 6f

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val needleRotation = remember { Animatable(rpmToAngle(TARGET_RPM)) }
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

        needleRotation.snapTo(rpmToAngle(TARGET_RPM))
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
