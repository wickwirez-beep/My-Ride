package com.wickwirez.myride.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DueStatusBeacon(dueStatus: DueStatus) {
    if (dueStatus != DueStatus.OVERDUE && dueStatus != DueStatus.DUE_SOON) return

    val label = if (dueStatus == DueStatus.OVERDUE) "Overdue" else "Due Soon"
    val color = if (dueStatus == DueStatus.OVERDUE) Color(0xFFD32F2F) else Color(0xFFF9A825)
    val periodMillis = if (dueStatus == DueStatus.OVERDUE) 450 else 1100

    Row(verticalAlignment = Alignment.CenterVertically) {
        PulsingBeacon(color = color, periodMillis = periodMillis)
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PulsingBeacon(color: Color, periodMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconScale"
    )

    Box(
        modifier = Modifier
            .size(14.dp)
            .scale(beaconScale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}
