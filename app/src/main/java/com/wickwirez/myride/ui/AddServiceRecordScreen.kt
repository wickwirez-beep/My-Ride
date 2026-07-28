package com.wickwirez.myride.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wickwirez.myride.R
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceRecordScreen(
    vehicleId: Long,
    currentMileage: Int,
    onSave: (ServiceRecord) -> Unit,
    onBack: () -> Unit
) {
    var type by remember { mutableStateOf(ServiceType.OIL_CHANGE) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var mileage by remember { mutableStateOf(if (currentMileage > 0) currentMileage.toString() else "") }
    var cost by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderMiles by remember { mutableStateOf("") }
    var reminderDays by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Service") },
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
                    .padding(16.dp)
            ) {
                Text(
                    "SERVICE TYPE",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        colors = glowFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        ServiceType.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.replace('_', ' ')) },
                                onClick = {
                                    type = option
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                LabeledIconField(
                    label = "Date",
                    value = SimpleDateFormat("MMM d, yyyy", Locale.US).format(dateMillis),
                    onValueChange = {},
                    icon = Icons.Default.CalendarToday,
                    readOnly = true,
                    trailingAction = { TextButton(onClick = { showDatePicker = true }) { Text("Change") } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                LabeledIconField(
                    label = "Mileage",
                    value = mileage,
                    onValueChange = { mileage = it },
                    icon = Icons.Default.Speed,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                LabeledIconField(
                    label = "Cost (Optional)",
                    value = cost,
                    onValueChange = { cost = it },
                    icon = Icons.Default.AttachMoney,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                LabeledIconField(
                    label = "Shop (Optional)",
                    value = shopName,
                    onValueChange = { shopName = it },
                    icon = Icons.Default.Store,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                LabeledIconField(
                    label = "Notes (Optional)",
                    value = notes,
                    onValueChange = { notes = it },
                    icon = Icons.Default.EditNote,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                SectionCard(Icons.Default.Notifications, "Remind me next time (optional)", null) {
                    LabeledIconField(
                        label = "Every ___ miles",
                        value = reminderMiles,
                        onValueChange = { reminderMiles = it },
                        keyboardType = KeyboardType.Number,
                        trailingText = "miles",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LabeledIconField(
                        label = "Or every ___ days",
                        value = reminderDays,
                        onValueChange = { reminderDays = it },
                        keyboardType = KeyboardType.Number,
                        trailingText = "days",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (showError) {
                    Text(
                        "Please enter a valid mileage.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val parsedMileage = mileage.toIntOrNull()
                            val parsedCost = cost.toDoubleOrNull() ?: 0.0

                            if (parsedMileage != null) {
                                showError = false
                                onSave(
                                    ServiceRecord(
                                        vehicleId = vehicleId,
                                        type = type,
                                        date = dateMillis,
                                        mileage = parsedMileage,
                                        cost = parsedCost,
                                        shopName = shopName.trim(),
                                        notes = notes.trim(),
                                        reminderIntervalMiles = reminderMiles.toIntOrNull(),
                                        reminderIntervalDays = reminderDays.toIntOrNull()
                                    )
                                )
                            } else {
                                showError = true
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.save_button_large),
                        contentDescription = "Save Service Record",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
