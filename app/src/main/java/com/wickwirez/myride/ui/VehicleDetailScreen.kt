package com.wickwirez.myride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextOverflow
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
    onDuplicateRecord: (ServiceRecord) -> Unit = {},
    onOpenAssistant: () -> Unit = {},
    onOpenRecalls: () -> Unit = {},
    onOpenFuelLog: () -> Unit = {},
    onOpenSpecs: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
    onOpenAiMechanic: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (vehicle == null) {
                            "Vehicle"
                        } else {
                            vehicle.nickname.ifBlank { "${vehicle.year} ${vehicle.make} ${vehicle.model}" }
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAssistant) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "AI Assistant")
                    }

                    if (vehicle != null) {
                        val context = LocalContext.current
                        var menuExpanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fuel Log") },
                                leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenFuelLog() }
                            )
                            DropdownMenuItem(
                                text = { Text("The Parts Store") },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenSpecs() }
                            )
                            DropdownMenuItem(
                                text = { Text("Documents") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenDocuments() }
                            )
                            DropdownMenuItem(
                                text = { Text("AI Mechanic") },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenAiMechanic() }
                            )
                            DropdownMenuItem(
                                text = { Text("Check for Recalls") },
                                leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenRecalls() }
                            )
                            DropdownMenuItem(
                                text = { Text("Print Service History") },
                                leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    PrintHelper.printServiceHistory(context, vehicle, records)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Summary") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    ShareHelper.shareVehicleSummary(context, vehicle, records)
                                }
                            )
                        }
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

                VehicleHeroSection(vehicle = vehicle, totalSpent = formatCost(totalCost))

                val healthScore = remember(vehicle, records) {
                    computeHealthScore(vehicle, records, System.currentTimeMillis())
                }
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    VehicleHealthGauge(score = healthScore, status = dueStatus)
                }

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
                        ServiceRecordRow(
                            record,
                            onClick = { onRecordClick(record) },
                            onDuplicate = { onDuplicateRecord(record) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRecordRow(record: ServiceRecord, onClick: () -> Unit, onDuplicate: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(record.type.name.replace('_', ' '), fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatCost(record.cost))
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate this entry",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
