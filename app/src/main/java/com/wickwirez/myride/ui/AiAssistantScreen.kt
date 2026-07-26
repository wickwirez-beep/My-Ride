package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.data.ApiKeyStore
import com.wickwirez.myride.data.ChatMessage
import com.wickwirez.myride.data.ClaudeApiClient
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    vehicle: Vehicle?,
    records: List<ServiceRecord>,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apiKey = remember { ApiKeyStore.getApiKey(context) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val systemPrompt = remember(vehicle, records) {
        if (vehicle == null) {
            ""
        } else {
            buildString {
                append("You are a helpful vehicle maintenance assistant inside the My Ride app. ")
                append("The user's vehicle: ${vehicle.year} ${vehicle.make} ${vehicle.model} ${vehicle.trim}. ")
                append("Current mileage: ${vehicle.currentMileage}. ")
                if (vehicle.vin.isNotBlank()) append("VIN: ${vehicle.vin}. ")
                if (records.isNotEmpty()) {
                    append("Service history: ")
                    records.take(20).forEach { r ->
                        append("[${r.type} at ${r.mileage} mi, cost $${r.cost}, notes: ${r.notes}] ")
                    }
                } else {
                    append("No service history logged yet. ")
                }
                append("Answer maintenance questions concisely and practically, using this context when relevant.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (apiKey.isNullOrBlank()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Add your Claude API key in Settings to use the assistant.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onOpenSettings) { Text("Open Settings") }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }
                }

                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about your vehicle…") },
                        enabled = !sending
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val userText = input.trim()
                            if (userText.isEmpty() || sending) return@IconButton
                            input = ""
                            error = null
                            val newHistory = messages + ChatMessage("user", userText)
                            messages = newHistory
                            sending = true
                            coroutineScope.launch {
                                val result = ClaudeApiClient.sendMessage(apiKey, systemPrompt, newHistory)
                                sending = false
                                result.onSuccess { reply ->
                                    messages = messages + ChatMessage("assistant", reply)
                                }.onFailure { e ->
                                    error = e.message ?: "Something went wrong"
                                }
                            }
                        },
                        enabled = !sending
                    ) {
                        if (sending) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                message.content,
                modifier = Modifier.padding(12.dp),
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
