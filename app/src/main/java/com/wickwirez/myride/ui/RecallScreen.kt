package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.data.RecallChecker
import com.wickwirez.myride.data.RecallInfo
import com.wickwirez.myride.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallScreen(vehicle: Vehicle?, onBack: () -> Unit) {
    var recalls by remember { mutableStateOf<List<RecallInfo>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vehicle?.id) {
        val v = vehicle
        if (v != null) {
            loading = true
            error = null
            val result = RecallChecker.fetchRecalls(v.make, v.model, v.year)
            loading = false
            result.onSuccess { recalls = it }
                .onFailure { error = it.message ?: "Couldn't check for recalls." }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recalls") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                recalls != null && recalls!!.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No open recalls found for this " +
                                "${vehicle?.year} ${vehicle?.make} ${vehicle?.model}."
                        )
                    }
                }
                recalls != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recalls!!) { recall ->
                            RecallCard(recall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecallCard(recall: RecallInfo) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                recall.component.ifBlank { "Recall ${recall.campaignNumber}" },
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Campaign ${recall.campaignNumber} • ${recall.reportDate}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Text(recall.summary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Remedy: ${recall.remedy}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
