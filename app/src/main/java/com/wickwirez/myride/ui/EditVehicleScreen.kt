package com.wickwirez.myride.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wickwirez.myride.data.VinDecoder
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleScreen(
    vehicle: Vehicle?,
    onSave: (Vehicle) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    if (vehicle == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Vehicle") },
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

    EditVehicleForm(vehicle = vehicle, onSave = onSave, onDelete = onDelete, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVehicleForm(
    vehicle: Vehicle,
    onSave: (Vehicle) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var nickname by remember(vehicle.id) { mutableStateOf(vehicle.nickname) }
    var year by remember(vehicle.id) { mutableStateOf(vehicle.year.toString()) }
    var make by remember(vehicle.id) { mutableStateOf(vehicle.make) }
    var model by remember(vehicle.id) { mutableStateOf(vehicle.model) }
    var trim by remember(vehicle.id) { mutableStateOf(vehicle.trim) }
    var vin by remember(vehicle.id) { mutableStateOf(vehicle.vin) }
    var currentMileage by remember(vehicle.id) { mutableStateOf(vehicle.currentMileage.toString()) }
    var photoUri by remember(vehicle.id) { mutableStateOf(vehicle.photoUri) }
    var decoding by remember { mutableStateOf(false) }
    var decodeError by remember { mutableStateOf<String?>(null) }
    var decodeSuccess by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Vehicle")
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Vehicle photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Add Photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Make") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = trim,
                onValueChange = { trim = it },
                label = { Text("Trim (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = vin,
                onValueChange = { vin = it.uppercase(); decodeError = null; decodeSuccess = null },
                label = { Text("VIN (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        decodeError = null
                        decoding = true
                        coroutineScope.launch {
                            val result = VinDecoder.decode(vin.trim())
                            decoding = false
                            if (result != null) {
                                if (result.year != null) year = result.year.toString()
                                if (result.make.isNotBlank()) make = result.make
                                if (result.model.isNotBlank()) model = result.model
                                if (result.trim.isNotBlank()) trim = result.trim
                                decodeSuccess = "Decoded: ${result.year ?: ""} ${result.make} ${result.model} ${result.trim}".trim()
                            } else {
                                decodeError = "Couldn't decode that VIN"
                            }
                        }
                    },
                    enabled = vin.trim().length == 17 && !decoding
                ) {
                    Text(if (decoding) "Decoding…" else "Decode VIN")
                }
                if (decoding) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            if (decodeError != null) {
                Text(
                    decodeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (decodeSuccess != null) {
                Text(
                    decodeSuccess!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = currentMileage,
                onValueChange = { currentMileage = it },
                label = { Text("Current Mileage") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = year.toIntOrNull() != null && make.isNotBlank() && model.isNotBlank(),
                onClick = {
                    val parsedYear = year.toIntOrNull() ?: return@Button
                    val mileage = currentMileage.toIntOrNull() ?: 0

                    onSave(
                        vehicle.copy(
                            nickname = nickname.trim(),
                            year = parsedYear,
                            make = make.trim(),
                            model = model.trim(),
                            trim = trim.trim(),
                            vin = vin.trim(),
                            currentMileage = mileage,
                            photoUri = photoUri
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Vehicle?") },
            text = { Text("This removes the vehicle and its full service history. This can't be undone.") },
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
