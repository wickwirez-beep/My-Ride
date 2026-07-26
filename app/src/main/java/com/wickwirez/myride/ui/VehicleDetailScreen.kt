package com.wickwirez.myride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicle: Vehicle?,
    records: List<ServiceRecord>,
    totalCost: Double,
    dueStatus: DueStatus,
    onAddService: () -> Unit,
    onRecordClick: (ServiceRecord) -> Unit,
    onOpenAssistant: () -> Unit = {},
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
                },
                actions = {
                    if (vehicle != null) {
                        val context = LocalContext.current
                        IconButton(onClick = { PrintHelper.printServiceHistory(context, vehicle, records) }) {
                            Icon(Icons.Default.Print, contentDescription = "Print service history")
                        }
                    }
                    IconButton(onClick = onOpenAssistant) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "AI Assistant")
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

                if (dueStatus == DueStatus.OVERDUE || dueStatus == DueStatus.DUE_SOON) {
                    Spacer(Modifier.height(8.dp))
                    DueStatusBeacon(dueStatus)
                }

                if (records.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    SpendingChart(records)
                }
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
                        ServiceRecordRow(record, onClick = { onRecordClick(record) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRecordRow(record: ServiceRecord, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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

@Composable
private fun SpendingChart(records: List<ServiceRecord>) {
    var monthLabels by remember { mutableStateOf(listOf<String>()) }
    val monthTotals = remember { mutableStateListOf<Double>() }

    LaunchedEffect(records) {
        val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.US)

        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -5)

        val keys = mutableListOf<String>()
        val labels = mutableListOf<String>()
        repeat(6) {
            keys.add(monthKeyFormat.format(cal.time))
            labels.add(monthLabelFormat.format(cal.time))
            cal.add(Calendar.MONTH, 1)
        }

        val totalsByKey = HashMap<String, Double>()
        keys.forEach { totalsByKey[it] = 0.0 }

        records.forEach { record ->
            val key = monthKeyFormat.format(Date(record.date))
            if (totalsByKey.containsKey(key)) {
                totalsByKey[key] = (totalsByKey[key] ?: 0.0) + record.cost
            }
        }

        monthTotals.clear()
        monthTotals.addAll(keys.map { totalsByKey[it] ?: 0.0 })
        monthLabels = labels
    }

    if (monthTotals.isEmpty()) return

    val maxValue = (monthTotals.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary

    Column {
        Text(
            "Spending (last 6 months)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(110.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            monthTotals.forEachIndexed { index, total ->
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val barFraction = (total / maxValue).toFloat().coerceIn(0f, 1f).coerceAtLeast(0.03f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(barFraction)
                            .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        monthLabels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
