package com.wickwirez.myride.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.wickwirez.myride.data.AppDatabase
import com.wickwirez.myride.data.DocumentStorage
import com.wickwirez.myride.model.DocumentCategories
import com.wickwirez.myride.model.VehicleDocument
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    vehicleId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).vehicleDocumentDao() }
    val documents by dao.getDocumentsForVehicle(vehicleId).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var pendingCategory by remember { mutableStateOf<String?>(null) }
    var viewerDocument by remember { mutableStateOf<VehicleDocument?>(null) }
    var deleteTarget by remember { mutableStateOf<VehicleDocument?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val category = pendingCategory
        if (uri != null && category != null) {
            coroutineScope.launch {
                val mimeType = context.contentResolver.getType(uri)
                val result = DocumentStorage.copyToInternalStorage(context, uri, mimeType)
                if (result != null) {
                    val (path, name) = result
                    dao.insertDocument(
                        VehicleDocument(
                            vehicleId = vehicleId,
                            category = category,
                            fileName = name,
                            filePath = path,
                            mimeType = mimeType ?: "application/octet-stream"
                        )
                    )
                }
            }
        }
        pendingCategory = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DocumentCategories.all.forEach { category ->
                val categoryDocs = documents.filter { it.category == category }

                SectionCard(
                    icon = Icons.Filled.Description,
                    title = category.uppercase()
                ) {
                    if (categoryDocs.isEmpty()) {
                        Text(
                            "No documents yet",
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 8.dp)
)
                    } else {
                        categoryDocs.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        if (doc.mimeType.startsWith("image/")) {
                                            viewerDocument = doc
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    doc.fileName,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { deleteTarget = doc }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = {
                        pendingCategory = category
                        pickerLauncher.launch("*/*")
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add to $category")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    viewerDocument?.let { doc ->
        FullScreenImageViewer(
            imagePath = doc.filePath,
            onDismiss = { viewerDocument = null }
        )
    }

    deleteTarget?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete document?") },
            text = { Text(doc.fileName) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        dao.deleteDocument(doc)
                        java.io.File(Uri.parse(doc.filePath).path ?: "").delete()
                    }
                    deleteTarget = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}


@Composable
fun FullScreenImageViewer(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 6f)
            offsetX += panChange.x
            offsetY += panChange.y
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Document",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .transformable(state = transformState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2.5f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        )
                    }
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}
