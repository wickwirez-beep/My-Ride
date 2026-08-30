package com.wickwirez.myride.ui

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wickwirez.myride.R
import com.wickwirez.myride.data.ApiKeyStore
import com.wickwirez.myride.data.GeminiApiClient
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMechanicScreen(
    vehicle: Vehicle?,
    records: List<ServiceRecord>,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apiKey = remember { ApiKeyStore.getApiKey(context) }
    val coroutineScope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var userNote by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf<String?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        diagnosis = null
        error = null
    }

    val systemPrompt = remember(vehicle, records) {
        if (vehicle == null) {
            ""
        } else {
            buildString {
                append("You are an expert automotive mechanic analyzing a photo for the My Ride app. ")
                append("The user's vehicle: ${vehicle.year} ${vehicle.make} ${vehicle.model} ${vehicle.trim}. ")
                append("Current mileage: ${vehicle.currentMileage}. ")
                if (records.isNotEmpty()) {
                    append("Recent service history: ")
                    records.take(10).forEach { r ->
                        append("[${r.type} at ${r.mileage} mi] ")
                    }
                }
                append("Identify any warning lights, visible damage, or issues in the photo. ")
                append("Explain what it likely means, how urgent it is, and recommended next steps. Be concise and practical.")
            }
        }
    }

    fun runDiagnosis() {
        val uri = photoUri ?: return
        val key = apiKey ?: return
        analyzing = true
        error = null
        diagnosis = null
        coroutineScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    error = "Couldn't read that photo."
                    analyzing = false
                    return@launch
                }
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val prompt = if (userNote.isBlank()) {
                    "Analyze this photo of my vehicle."
                } else {
                    "Analyze this photo of my vehicle. Additional context from me: $userNote"
                }
                val result = GeminiApiClient.sendImageDiagnosis(key, systemPrompt, prompt, base64, mimeType)
                result.onSuccess { diagnosis = it }
                result.onFailure { error = it.message ?: "Diagnosis failed." }
            } catch (e: Exception) {
                error = e.message ?: "Diagnosis failed."
            } finally {
                analyzing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Mechanic") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (apiKey.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Add your free Gemini API key in Settings to use the AI Mechanic.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onOpenSettings) { Text("Open Settings") }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.artwork_ai_mechanic_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Selected photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Choose Photo")
                        }
                    }
                }

                if (photoUri != null) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Text("Choose a different photo")
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = userNote,
                    onValueChange = { userNote = it },
                    label = { Text("Describe the issue") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { runDiagnosis() },
                    enabled = photoUri != null && !analyzing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (analyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Diagnose")
                    }
                }

                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                diagnosis?.let {
                    Spacer(Modifier.height(20.dp))
                    Text("Diagnosis", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    MarkdownText(text = it)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
