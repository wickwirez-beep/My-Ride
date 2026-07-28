package com.wickwirez.myride.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wickwirez.myride.R
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    vehicles: List<VehicleWithStatus>,
    onAddVehicle: () -> Unit,
    onVehicleClick: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit = {},
    onEditVehicle: (Vehicle) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {

    Box {
        Image(
            painter = painterResource(id = R.drawable.tachometer_photo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(Color(0x99000000))
        )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Ride",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Image(
                            painter = painterResource(id = R.drawable.nav_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddVehicle,
                icon = { Icon(Icons.Default.Add, null, tint = Color.White) },
                text = { Text("Add Vehicle", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->

        if (vehicles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.nav_garage),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text("Your Garage is Empty", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap Add Vehicle to begin.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(vehicles) { index, entry ->
                    VehicleCard(
                        vehicle = entry.vehicle,
                        dueStatus = entry.dueStatus,
                        index = index,
                        onClick = { onVehicleClick(entry.vehicle) },
                        onDelete = { onDeleteVehicle(entry.vehicle) },
                        onEdit = { onEditVehicle(entry.vehicle) }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    dueStatus: DueStatus,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 70L)
        appeared = true
    }

    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        GlowCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                            Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (vehicle.nickname.isBlank()) {
                                "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                            } else {
                                vehicle.nickname
                            },
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.7f))
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

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${vehicle.currentMileage} miles", color = Color.White.copy(alpha = 0.9f))
                }

                if (vehicle.vin.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "VIN: ${vehicle.vin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                if (dueStatus == DueStatus.OVERDUE || dueStatus == DueStatus.DUE_SOON) {
                    Spacer(Modifier.height(10.dp))
                    DueStatusBeacon(dueStatus)
                }
            }
        }
    }
}
