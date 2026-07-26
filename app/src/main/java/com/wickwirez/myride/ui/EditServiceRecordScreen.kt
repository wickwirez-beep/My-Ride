package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceRecordScreen(
    record: ServiceRecord?,
    onSave: (ServiceRecord) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    if (record == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Service") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Service record not found.")
            }
        }
        return
    }

    EditServiceRecordForm(record = record, onSave = onSave, onDelete = onDelete, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServiceRecordForm(
    record: ServiceRecord,
    onSave: (ServiceRecord) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var type by remember(record.id) { mutableStateOf(record.type) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var dateMillis by remember(record.id) { mutableStateOf(record.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var mileage by remember(record.id) { mutableStateOf(record.mileage.toString()) }
    var cost by remember(record.id) { mutableStateOf(if (record.cost > 0.0) record.cost.toString() else "") }
    var shopName by remember(record.id) { mutableStateOf(record.shopName) }
    var notes by remember(record.id) { mutableStateOf(record.notes) }
    var reminderMiles by remember(record.id) { mutableStateOf(record.reminderIntervalMiles?.toString() ?: "") }
    var reminderDays by remember(record.id) { mutableStateOf(record.reminderIntervalDays?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Service") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Service Record")
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

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = type.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
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

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = SimpleDateFormat("MMM d, yyyy", Locale.US).format(dateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Change") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("Mileage") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Remind me next time (optional)", style = MaterialTheme.typography.labelLarge)

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = reminderMiles,
                onValueChange = { reminderMiles = it },
                label = { Text("Every ___ miles") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = reminderDays,
                onValueChange = { reminderDays = it },
                label = { Text("Or every ___ days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (showError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Please enter a valid mileage.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val parsedMileage = mileage.toIntOrNull()
                    val parsedCost = cost.toDoubleOrNull() ?: 0.0

                    if (parsedMileage != null) {
                        showError = false
                        onSave(
                            record.copy(
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
                Text("Save Changes")
            }

            Spacer(Modifier.height(24.dp))
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Service Record?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
