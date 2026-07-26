package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicle: Vehicle?,
    records: List<ServiceRecord>,
    totalCost: Double,
    onAddService: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (vehicle == null) "Vehicle"
                        else vehicle.nickname.ifBlank { "${vehicle.year} ${vehicle.make} ${vehicle.model}" }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddService,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Log Service") }
            )
        }
    ) { padding ->

        if (vehicle == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Vehicle not found.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.padding(16.dp)) {

                if (vehicle.photoUri != null) {
                    AsyncImage(
                        model = vehicle.photoUri,
                        contentDescription = "Vehicle photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    "${vehicle.year} ${vehicle.make} ${vehicle.model}${if (vehicle.trim.isNotBlank()) " ${vehicle.trim}" else ""}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("${vehicle.currentMileage} miles")
                if (vehicle.vin.isNotBlank()) {
                    Text("VIN: ${vehicle.vin}")
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "Total spent: ${formatCost(totalCost)}", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider()

            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No service history yet. Tap Log Service to add the first entry.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = records, key = { it.id }) { record ->
                        ServiceRecordRow(record)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRecordRow(record: ServiceRecord) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.type.name.replace('_', ' '), fontWeight = FontWeight.Bold)
                Text(formatCost(record.cost))
            }
            Spacer(Modifier.height(4.dp))
            Text("${formatDate(record.date)} • ${record.mileage} mi")
            if (record.shopName.isNotBlank()) {
                Text(record.shopName)
            }
            if (record.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(record.notes)
            }
        }
    }
}

private fun formatCost(cost: Double): String =
    if (cost <= 0.0) "—" else String.format(Locale.US, "$%.2f", cost)

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.US).format(epochMillis)
