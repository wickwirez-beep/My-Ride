package com.wickwirez.myride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wickwirez.myride.R
import com.wickwirez.myride.model.Vehicle

private fun heroArtworkFor(vehicle: Vehicle): Int? = when {
    vehicle.nickname.equals("Black Knight", ignoreCase = true) -> R.drawable.artwork_truck_hero
    vehicle.nickname.equals("Sandy", ignoreCase = true) -> R.drawable.artwork_bronco_hero
    else -> null
}

@Composable
fun VehicleHeroSection(
    vehicle: Vehicle,
    totalSpent: String,
    parallaxOffsetPx: Float = 0f
) {
    val heroRes = heroArtworkFor(vehicle)
    val imageModel: Any? = heroRes ?: vehicle.photoUri

    Column(modifier = Modifier.fillMaxWidth()) {
        if (imageModel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "${vehicle.nickname} photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = -parallaxOffsetPx * 0.4f
                            scaleX = 1.15f
                            scaleY = 1.15f
                        },
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        if (vehicle.nickname.isNotBlank()) {
            Text(
                text = vehicle.nickname.uppercase(),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFFE53935).copy(alpha = 0.85f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                        blurRadius = 24f
                    )
                )
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(3.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(10.dp))
        }
        Text(
            text = "${vehicle.year} ${vehicle.make} ${vehicle.model}${if (vehicle.trim.isNotBlank()) " ${vehicle.trim}" else ""}".uppercase(),
            color = Color(0xFFE0E0E0),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${vehicle.currentMileage} MILES",
            color = Color(0xFFE53935),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))

        if (vehicle.vin.isNotBlank()) {
            Text("VIN: ${vehicle.vin}")
        }
    }
}
