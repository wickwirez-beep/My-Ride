package com.wickwirez.myride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    else -> null
}

@Composable
fun VehicleHeroSection(vehicle: Vehicle, totalSpent: String) {
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
                    modifier = Modifier.fillMaxSize(),
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
                text = vehicle.nickname,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "${vehicle.year} ${vehicle.make} ${vehicle.model}${if (vehicle.trim.isNotBlank()) " ${vehicle.trim}" else ""}",
            color = Color(0xFFE0E0E0),
            fontSize = 15.sp
        )
        Text(
            text = "${vehicle.currentMileage} miles",
            color = Color(0xFFB3B3B3),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))

        if (vehicle.vin.isNotBlank()) {
            Text("VIN: ${vehicle.vin}")
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "Total spent: $totalSpent", fontWeight = FontWeight.Bold)
    }
}
