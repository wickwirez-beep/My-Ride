package com.wickwirez.myride.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val SWEEP_START = 135f
private const val SWEEP_MAX = 270f

private fun colorForStatus(status: DueStatus): Color = when (status) {
    DueStatus.OVERDUE -> Color(0xFFE53935)
    DueStatus.DUE_SOON -> Color(0xFFFFC107)
    DueStatus.OK -> Color(0xFF3DDC84)
    DueStatus.NONE -> Color(0xFF8A8F98)
}

private fun labelForStatus(status: DueStatus): String = when (status) {
    DueStatus.OVERDUE -> "Overdue"
    DueStatus.DUE_SOON -> "Due Soon"
    DueStatus.OK -> "Healthy"
    DueStatus.NONE -> "No Data"
}

@Composable
fun VehicleHealthGauge(
    score: Int,
    status: DueStatus,
    modifier: Modifier = Modifier,
    diameter: Dp = 160.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val hasData = status != DueStatus.NONE
    val targetFraction = if (hasData) score.coerceIn(0, 100) / 100f else 0f
    val color = if (hasData) colorForStatus(status) else Color(0xFF4A4F58)

    LaunchedEffect(score) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = targetFraction,
            animationSpec = tween(durationMillis = 1100)
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VEHICLE HEALTH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A8F98),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier.size(diameter),
            contentAlignment = Alignment.Center
        ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokeWidth = diameter.toPx() * 0.09f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = SWEEP_START,
                sweepAngle = SWEEP_MAX,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = SWEEP_START,
                sweepAngle = SWEEP_MAX * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasData) "$score" else "—",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = labelForStatus(status),
                fontSize = 12.sp,
                color = Color(0xFFB3B3B3)
            )
        }
        }
    }
}
