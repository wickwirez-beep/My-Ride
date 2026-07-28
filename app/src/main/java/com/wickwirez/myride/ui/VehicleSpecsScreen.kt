package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.R
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
    var oilFilterBrand by remember(vehicle.id) { mutableStateOf(vehicle.oilFilterBrand) }
    var oilFilterPartNumber by remember(vehicle.id) { mutableStateOf(vehicle.oilFilterPartNumber) }
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
            SectionHeader(Icons.Default.Build, "Oil", R.drawable.oil_pour)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = oilType,
                onValueChange = { oilType = it; saved = false },
                label = { Text("Oil Type") },
                placeholder = { Text("e.g. 5W-30 Full Synthetic") },
                leadingIcon = { Icon(Icons.Default.Opacity, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = oilCapacity,
                onValueChange = { oilCapacity = it; saved = false },
                label = { Text("Oil Capacity (with filter)") },
                placeholder = { Text("e.g. 6.0 qts") },
                leadingIcon = { Icon(Icons.Default.LocalDrink, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(Icons.Default.FilterAlt, "Oil Filter", R.drawable.oil_bottle)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = oilFilterBrand,
                onValueChange = { oilFilterBrand = it; saved = false },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = oilFilterPartNumber,
                onValueChange = { oilFilterPartNumber = it; saved = false },
                label = { Text("Part Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(Icons.Default.FilterAlt, "Air Filter", R.drawable.air_filter_photo)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = airFilterPartNumber,
                onValueChange = { airFilterPartNumber = it; saved = false },
                label = { Text("Part Number") },
                placeholder = { Text("Enter part number...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(Icons.Default.Bolt, "Spark Plugs", R.drawable.spark_plug_photo)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = sparkPlugType,
                onValueChange = { sparkPlugType = it; saved = false },
                label = { Text("Type / Part Number") },
                placeholder = { Text("Enter type or part number...") },
                leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = sparkPlugGap,
                onValueChange = { sparkPlugGap = it; saved = false },
                label = { Text("Gap") },
                placeholder = { Text("e.g. 0.028 in") },
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            SectionHeader(Icons.Default.EditNote, "Other Notes", R.drawable.carbon_fiber_panel)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = specNotes,
                onValueChange = { specNotes = it; saved = false },
                placeholder = { Text("Anything else (tire size, fluids, torque specs...)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            CheckeredFlagButton(
                text = "Save Specs",
                onClick = {
                    onSave(
                        vehicle.copy(
                            oilType = oilType.trim(),
                            oilCapacity = oilCapacity.trim(),
                            oilFilterBrand = oilFilterBrand.trim(),
                            oilFilterPartNumber = oilFilterPartNumber.trim(),
                            airFilterPartNumber = airFilterPartNumber.trim(),
                            sparkPlugType = sparkPlugType.trim(),
                            sparkPlugGap = sparkPlugGap.trim(),
                            specNotes = specNotes.trim()
                        )
                    )
                    saved = true
                }
            )

            if (saved) {
                Spacer(Modifier.height(8.dp))
                Text("Saved.", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
