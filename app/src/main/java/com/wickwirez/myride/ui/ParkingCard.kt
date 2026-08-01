package com.wickwirez.myride.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wickwirez.myride.data.LocationHelper
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun ParkingCard(
    parkedLat: Double?,
    parkedLng: Double?,
    parkedAt: Long?,
    onSpotMarked: (Double, Double, Long) -> Unit,
    onSpotCleared: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var requesting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            requesting = true
            coroutineScope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                requesting = false
                if (result != null) {
                    onSpotMarked(result.first, result.second, System.currentTimeMillis())
                }
            }
        }
    }

    fun markSpot() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            requesting = true
            coroutineScope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                requesting = false
                if (result != null) {
                    onSpotMarked(result.first, result.second, System.currentTimeMillis())
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE53935))
                Spacer(Modifier.width(8.dp))
                Text("SATELLITE PARKING", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(12.dp))

            if (parkedLat == null || parkedLng == null) {
                Button(
                    onClick = { markSpot() },
                    enabled = !requesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (requesting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Mark My Spot")
                    }
                }
            } else {
                Text(
                    text = "Parked ${formatElapsed(parkedAt)}",
                    color = Color(0xFFB3B3B3)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { openMapsUrl(context, "https://www.google.com/maps/@$parkedLat,$parkedLng,19z/data=!3m1!1e3") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Satellite", fontSize = 12.sp) }
                    OutlinedButton(
                        onClick = { openMapsUrl(context, "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=$parkedLat,$parkedLng") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Street", fontSize = 12.sp) }
                    OutlinedButton(
                        onClick = { openMapsUrl(context, "https://www.google.com/maps/dir/?api=1&destination=$parkedLat,$parkedLng&travelmode=walking") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Walk", fontSize = 12.sp) }
                    OutlinedButton(
                        onClick = onSpotCleared,
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Default.Close, contentDescription = "Clear parked spot", modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

private fun openMapsUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun formatElapsed(parkedAt: Long?): String {
    if (parkedAt == null) return ""
    val diffMs = System.currentTimeMillis() - parkedAt
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hr ago"
        else -> "${minutes / 1440} days ago"
    }
}
