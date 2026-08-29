package com.wickwirez.myride.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wickwirez.myride.data.ApiKeyStore
import com.wickwirez.myride.data.GeminiApiClient
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.ServiceType
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoServiceRecordScreen(
    vehicleId: Long,
    currentMileage: Int,
    onOpenSettings: () -> Unit,
    onSave: (ServiceRecord) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apiKey = remember { ApiKeyStore.getApiKey(context) }
    val coroutineScope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var parsed by remember { mutableStateOf<ParsedReceipt?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        error = null
    }

    val systemPrompt = "You are extracting structured data from a photo of an auto service receipt or invoice for the My Ride app. " +
        "Respond with ONLY a single JSON object, no markdown fences, no extra text, using exactly these keys: " +
        "shopName (string, empty if unknown), date (string in yyyy-MM-dd format, empty if unknown), " +
        "mileage (number, 0 if unknown), cost (number, total amount charged, 0 if unknown), " +
        "serviceType (one of: OIL_CHANGE, TIRE_ROTATION, TIRE_REPLACEMENT, BRAKES, BATTERY, INSPECTION, REGISTRATION, FLUID_CHANGE, FILTER_CHANGE, REPAIR, OTHER -- pick the closest match), " +
        "notes (string, a brief one sentence summary of the work done, empty if unclear)."

    fun runExtraction() {
        val uri = photoUri ?: return
        val key = apiKey ?: return
        analyzing = true
        error = null
        coroutineScope.launch {
            try {
                val compressedBytes = resizeAndCompressImage(context, uri)
                if (compressedBytes == null) {
                    error = "Couldn't read that photo."
                    analyzing = false
                    return@launch
                }
                val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                val mimeType = "image/jpeg"
                val result = GeminiApiClient.sendImageDiagnosis(
                    key, systemPrompt, "Extract the service details from this receipt photo.", base64, mimeType
                )
                result.onSuccess { raw ->
                    val extracted = parseReceiptJson(raw)
                    if (extracted == null) {
                        error = "Couldn't read the receipt clearly. You can still fill it in manually below."
                        parsed = ParsedReceipt()
                    } else {
                        parsed = extracted
                    }
                }
                result.onFailure { error = it.message ?: "Extraction failed." }
            } catch (e: Exception) {
                error = e.message ?: "Extraction failed."
            } finally {
                analyzing = false
            }
        }
    }

    val currentParsed = parsed
    if (currentParsed != null) {
        AddServiceRecordScreen(
            vehicleId = vehicleId,
            currentMileage = currentMileage,
            initialType = currentParsed.type,
            initialDateMillis = currentParsed.dateMillis,
            initialMileage = currentParsed.mileage,
            initialCost = currentParsed.cost,
            initialShopName = currentParsed.shopName,
            initialNotes = currentParsed.notes,
            onSave = onSave,
            onBack = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Service Log") },
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
                    Text("Add your free Gemini API key in Settings to use Photo Service Log.")
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
                Text("Take or choose a photo of the receipt, and I'll fill in the details for you.")
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
                            contentDescription = "Selected receipt photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Choose Receipt Photo")
                        }
                    }
                }

                if (photoUri != null) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Text("Choose a different photo")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { runExtraction() },
                    enabled = photoUri != null && !analyzing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (analyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Extract Details")
                    }
                }

                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun resizeAndCompressImage(context: android.content.Context, uri: Uri): ByteArray? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val original = input.use { BitmapFactory.decodeStream(it) } ?: return null
        val maxDim = 1600
        val longestSide = maxOf(original.width, original.height)
        val scale = if (longestSide > maxDim) maxDim.toFloat() / longestSide else 1f
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
        } else {
            original
        }
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, output)
        output.toByteArray()
    } catch (e: Exception) {
        null
    }
}

private data class ParsedReceipt(
    val type: ServiceType? = null,
    val dateMillis: Long? = null,
    val mileage: Int? = null,
    val cost: Double? = null,
    val shopName: String = "",
    val notes: String = ""
)

private fun parseReceiptJson(raw: String): ParsedReceipt? {
    return try {
        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(cleaned)

        val type = json.optString("serviceType", "").uppercase().let { name ->
            try { ServiceType.valueOf(name) } catch (e: Exception) { null }
        }

        val dateMillis = json.optString("date", "").takeIf { it.isNotBlank() }?.let { dateStr ->
            try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)?.time } catch (e: Exception) { null }
        }

        val mileage = json.optInt("mileage", 0).takeIf { it > 0 }
        val cost = json.optDouble("cost", 0.0).takeIf { it > 0 }
        val shopName = json.optString("shopName", "")
        val notes = json.optString("notes", "")

        ParsedReceipt(type, dateMillis, mileage, cost, shopName, notes)
    } catch (e: Exception) {
        null
    }
}
