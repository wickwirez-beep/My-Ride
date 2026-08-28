package com.wickwirez.myride.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.android.play.core.review.ReviewManagerFactory
import com.wickwirez.myride.data.ApiKeyStore
import com.wickwirez.myride.data.BackupManager
import com.wickwirez.myride.data.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun openPlayStoreListing(context: Context) {
    val packageName = "com.wickwirez.myride" // always point at the published listing, even from a .tester/.debug build
    android.widget.Toast.makeText(context, "DEBUG: opening id=$packageName (running as ${context.packageName})", android.widget.Toast.LENGTH_LONG).show()
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .setPackage("com.android.vending")
        )
    } catch (e: ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "DEBUG: market:// failed, using web fallback", android.widget.Toast.LENGTH_LONG).show()
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        )
    }
}

private fun requestInAppReview(context: Context) {
    val activity = context.findActivity()
    if (activity == null) {
        openPlayStoreListing(context)
        return
    }
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            manager.launchReviewFlow(activity, reviewInfo)
            // No completion callback is used here on purpose: Google's API contract
            // doesn't guarantee the dialog was actually shown (it's quota-limited),
            // so there's nothing reliable to react to either way.
        } else {
            openPlayStoreListing(context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: VehicleRepository,
    onOpenHelp: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: () -> Unit
) {
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
                try {
                    val vehicles = repository.getAllVehiclesOnce()
                    val records = repository.getAllRecordsOnce()
                    val fuelLogs = repository.getAllFuelLogsOnce()
                    val json = BackupManager.buildBackupJson(vehicles, records, fuelLogs, ApiKeyStore.getApiKey(context))
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    }
                    backupStatus = "Backup saved: ${vehicles.size} vehicle(s), " +
                        "${records.size} record(s), ${fuelLogs.size} fuel log(s)."
                } catch (e: Throwable) {
                    backupStatus = "Backup failed: ${e::class.simpleName}: ${e.message}"
                } finally {
                    working = false
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            working = true
            coroutineScope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    val data = json?.let { BackupManager.parseBackupJson(context, it) }
                    if (data != null) {
                        data.vehicles.forEach { repository.addVehicle(it) }
                        data.records.forEach { repository.addServiceRecord(it) }
                        data.fuelLogs.forEach { repository.addFuelLog(it) }
                        data.apiKey?.let { key -> ApiKeyStore.setApiKey(context, key); apiKey = key }
                        backupStatus = "Restored ${data.vehicles.size} vehicle(s), " +
                            "${data.records.size} record(s), ${data.fuelLogs.size} fuel log(s)."
                    } else {
                        backupStatus = "That file couldn't be read as a My Ride backup."
                    }
                } catch (e: Throwable) {
                    backupStatus = "Restore failed: ${e::class.simpleName}: ${e.message}"
                } finally {
                    working = false
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
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
                    onClick = {
                        try {
                            backupLauncher.launch("myride-backup.json")
                        } catch (e: Throwable) {
                            backupStatus = "Couldn't open save dialog: ${e::class.simpleName}: ${e.message}"
                        }
                    },
                    enabled = !working
                ) {
                    Text("Back Up")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            restoreLauncher.launch(arrayOf("application/json"))
                        } catch (e: Throwable) {
                            backupStatus = "Couldn't open file picker: ${e::class.simpleName}: ${e.message}"
                        }
                    },
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

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("Enjoying My Ride?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "If it's been useful, a quick rating helps a lot.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { openPlayStoreListing(context) }) {
                Text("Rate My Ride")
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Help & Support",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onOpenHelp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Help & FAQ")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onReplayOnboarding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Replay Welcome Tour")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onOpenAbout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("About My Ride")
            }
        }
    }
}
