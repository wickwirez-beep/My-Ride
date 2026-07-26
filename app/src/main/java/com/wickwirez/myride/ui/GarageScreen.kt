package com.wickwirez.myride.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wickwirez.myride.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    vehicles: List<VehicleWithStatus>,
    onAddVehicle: () -> Unit,
    onVehicleClick: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit = {},
    onEditVehicle: (Vehicle) -> Unit = {}
) {

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddVehicle,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Vehicle") }
            )
        }
    ) { padding ->

        if (vehicles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("Your Garage is Empty", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap Add Vehicle to begin.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = vehicles, key = { it.vehicle.id }) { entry ->
                    VehicleCard(
                        vehicle = entry.vehicle,
                        dueStatus = entry.dueStatus,
                        onClick = { onVehicleClick(entry.vehicle) },
                        onDelete = { onDeleteVehicle(entry.vehicle) },
                        onEdit = { onEditVehicle(entry.vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    dueStatus: DueStatus,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (vehicle.photoUri != null) {
                        AsyncImage(
                            model = vehicle.photoUri,
                            contentDescription = "Vehicle photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.DirectionsCar, null)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (vehicle.nickname.isBlank())
                            "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                        else
                            vehicle.nickname,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${vehicle.year} ${vehicle.make} ${vehicle.model}")
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, null)
                Spacer(Modifier.width(8.dp))
                Text("${vehicle.currentMileage} miles")
            }

            if (vehicle.vin.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("VIN: ${vehicle.vin}")
            }

            if (dueStatus == DueStatus.OVERDUE || dueStatus == DueStatus.DUE_SOON) {
                Spacer(Modifier.height(10.dp))
                val label = if (dueStatus == DueStatus.OVERDUE) "Overdue" else "Due Soon"
                val color = if (dueStatus == DueStatus.OVERDUE) Color(0xFFD32F2F) else Color(0xFFF9A825)
                val periodMillis = if (dueStatus == DueStatus.OVERDUE) 450 else 1100

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingBeacon(color = color, periodMillis = periodMillis)
                    Spacer(Modifier.width(8.dp))
                    Text(label, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }
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
