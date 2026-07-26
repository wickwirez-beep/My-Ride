package com.wickwirez.myride.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.data.ApiKeyStore
import com.wickwirez.myride.data.BackupManager
import com.wickwirez.myride.data.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: VehicleRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(ApiKeyStore.getApiKey(context) ?: "") }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            working = true
            coroutineScope.launch {
                val vehicles = repository.getAllVehiclesOnce()
                val records = repository.getAllRecordsOnce()
                val json = BackupManager.buildBackupJson(vehicles, records)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
                working = false
                backupStatus = "Backup saved: ${vehicles.size} vehicle(s), ${records.size} record(s)."
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            working = true
            coroutineScope.launch {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }
                val data = json?.let { BackupManager.parseBackupJson(it) }
                working = false
                if (data != null) {
                    data.vehicles.forEach { repository.addVehicle(it) }
                    data.records.forEach { repository.addServiceRecord(it) }
                    backupStatus = "Restored ${data.vehicles.size} vehicle(s), ${data.records.size} record(s)."
                } else {
                    backupStatus = "That file couldn't be read as a My Ride backup."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(20.dp)
        ) {
            Text("Gemini API Key", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Used for the AI Assistant on each vehicle. Get a free key at " +
                    "aistudio.google.com/apikey — no credit card needed. " +
                    "Stored encrypted on this device only.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                label = { Text("API Key") },
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "Hide" else "Show")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    ApiKeyStore.setApiKey(context, apiKey.trim())
                    saved = true
                },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }

            if (saved) {
                Spacer(Modifier.height(8.dp))
                Text("Saved.", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Save all your vehicles and service history to a file, or restore " +
                    "from one — useful if the app ever gets reinstalled.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))

            Row {
                Button(
                    onClick = { backupLauncher.launch("myride-backup.json") },
                    enabled = !working
                ) {
                    Text("Back Up")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                    enabled = !working
                ) {
                    Text("Restore")
                }
            }

            if (working) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }

            if (backupStatus != null) {
                Spacer(Modifier.height(12.dp))
                Text(backupStatus!!, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
