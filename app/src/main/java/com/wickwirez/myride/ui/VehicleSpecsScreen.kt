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
import androidx.compose.ui.text.input.KeyboardType
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
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            EdgeGlow()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(Icons.Default.Build, "Oil", R.drawable.oil_pour) {
                    LabeledIconField(
                        label = "Oil Type",
                        value = oilType,
                        onValueChange = { oilType = it; saved = false },
                        icon = Icons.Default.Opacity,
                        placeholder = "e.g. 5W-30 Full Synthetic",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LabeledIconField(
                        label = "Oil Capacity (with filter)",
                        value = oilCapacity,
                        onValueChange = { oilCapacity = it; saved = false },
                        icon = Icons.Default.LocalDrink,
                        placeholder = "e.g. 6.0 qts",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SectionCard(Icons.Default.FilterAlt, "Oil Filter", R.drawable.oil_bottle) {
                    LabeledIconField(
                        label = "Brand",
                        value = oilFilterBrand,
                        onValueChange = { oilFilterBrand = it; saved = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LabeledIconField(
                        label = "Part Number",
                        value = oilFilterPartNumber,
                        onValueChange = { oilFilterPartNumber = it; saved = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SectionCard(Icons.Default.FilterAlt, "Air Filter", R.drawable.air_filter_photo) {
                    LabeledIconField(
                        label = "Part Number",
                        value = airFilterPartNumber,
                        onValueChange = { airFilterPartNumber = it; saved = false },
                        placeholder = "Enter part number...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SectionCard(Icons.Default.Bolt, "Spark Plugs", R.drawable.spark_plug_photo) {
                    LabeledIconField(
                        label = "Type / Part Number",
                        value = sparkPlugType,
                        onValueChange = { sparkPlugType = it; saved = false },
                        icon = Icons.Default.Bolt,
                        placeholder = "Enter type or part number...",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LabeledIconField(
                        label = "Gap",
                        value = sparkPlugGap,
                        onValueChange = { sparkPlugGap = it; saved = false },
                        icon = Icons.Default.Straighten,
                        placeholder = "e.g. 0.028 in",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SectionCard(Icons.Default.EditNote, "Other Notes", R.drawable.carbon_fiber_panel) {
                    OutlinedTextField(
                        value = specNotes,
                        onValueChange = { specNotes = it; saved = false },
                        placeholder = { Text("Anything else (tire size, fluids, torque specs...)", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.35f)) },
                        minLines = 3,
                        colors = glowFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(4.dp))


                if (saved) {
                    Text("Saved.", color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
