package com.wickwirez.myride.ui

import android.media.MediaPlayer
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.wickwirez.myride.R

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
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
        delay(soundDurationMs)

        alpha.animateTo(0f, animationSpec = tween(durationMillis = 400))
        onFinished()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Image(
            painter = painterResource(id = R.drawable.splash_poster),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value),
            contentScale = ContentScale.Fit
        )
    }
}
