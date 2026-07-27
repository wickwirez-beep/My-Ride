package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSpecsScreen(
    vehicle: Vehicle?,
    onSave: (Vehicle) -> Unit,
    onBack: () -> Unit
) {
    if (vehicle == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("The Parts Store") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Vehicle not found.")
            }
        }
        return
    }

    VehicleSpecsForm(vehicle = vehicle, onSave = onSave, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleSpecsForm(
    vehicle: Vehicle,
    onSave: (Vehicle) -> Unit,
    onBack: () -> Unit
) {
    var oilType by remember(vehicle.id) { mutableStateOf(vehicle.oilType) }
    var oilCapacity by remember(vehicle.id) { mutableStateOf(vehicle.oilCapacity) }
    var airFilterPartNumber by remember(vehicle.id) { mutableStateOf(vehicle.airFilterPartNumber) }
    var sparkPlugType by remember(vehicle.id) { mutableStateOf(vehicle.sparkPlugType) }
    var sparkPlugGap by remember(vehicle.id) { mutableStateOf(vehicle.sparkPlugGap) }
    var specNotes by remember(vehicle.id) { mutableStateOf(vehicle.specNotes) }
    var saved by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Parts Store") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text("Oil", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = oilType,
                onValueChange = { oilType = it; saved = false },
                label = { Text("Oil Type (e.g. 5W-30 Synthetic)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = oilCapacity,
                onValueChange = { oilCapacity = it; saved = false },
                label = { Text("Oil Capacity (e.g. 6.0 qts with filter)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text("Air Filter", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = airFilterPartNumber,
                onValueChange = { airFilterPartNumber = it; saved = false },
                label = { Text("Part Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text("Spark Plugs", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = sparkPlugType,
                onValueChange = { sparkPlugType = it; saved = false },
                label = { Text("Type / Part Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = sparkPlugGap,
                onValueChange = { sparkPlugGap = it; saved = false },
                label = { Text("Gap (e.g. 0.028 in)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text("Other Notes", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = specNotes,
                onValueChange = { specNotes = it; saved = false },
                label = { Text("Anything else (tire size, fluids, torque specs...)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onSave(
                        vehicle.copy(
                            oilType = oilType.trim(),
                            oilCapacity = oilCapacity.trim(),
                            airFilterPartNumber = airFilterPartNumber.trim(),
                            sparkPlugType = sparkPlugType.trim(),
                            sparkPlugGap = sparkPlugGap.trim(),
                            specNotes = specNotes.trim()
                        )
                    )
                    saved = true
                }
            ) {
                Text("Save Specs")
            }

            if (saved) {
                Spacer(Modifier.height(8.dp))
                Text("Saved.", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
