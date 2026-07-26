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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wickwirez.myride.data.PhotoStorage
import com.wickwirez.myride.data.VinDecoder
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onSave: (Vehicle) -> Unit,
    onBack: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var trim by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var currentMileage by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var decoding by remember { mutableStateOf(false) }
    var decodeError by remember { mutableStateOf<String?>(null) }
    var decodeSuccess by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = PhotoStorage.copyToInternalStorage(context, uri)
                if (savedPath != null) {
                    photoUri = savedPath
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                                if (year.isBlank() && result.year != null) year = result.year.toString()
                                if (make.isBlank()) make = result.make
                                if (model.isBlank()) model = result.model
                                if (trim.isBlank()) trim = result.trim
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
                label = { Text("Current Mileage (Optional)") },
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
                        Vehicle(
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
                Text("Save Vehicle")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
