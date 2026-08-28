package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onReplayWelcomeTour: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ") },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Open your Garage, add a vehicle, then tap its card to reach maintenance, fuel, documents, recalls, AI tools, and parking."
            )

            Button(
                onClick = onReplayWelcomeTour,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Replay Welcome Tour")
            }

            HorizontalDivider()

            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleLarge
            )

            HelpCard(
                question = "How do I add or edit a vehicle?",
                answer = "From the Garage, tap Add Vehicle. Enter the details manually or use the VIN scanner. To make changes later, use the edit option on that vehicle."
            )

            HelpCard(
                question = "How do I record maintenance?",
                answer = "Open a vehicle and choose Add Service. Enter the service type, mileage, cost, date, and any notes you want to keep."
            )

            HelpCard(
                question = "Where do I store registration and insurance?",
                answer = "Open the vehicle and select Digital Glove Box. You can keep important vehicle documents together and easy to find."
            )

            HelpCard(
                question = "How do I use AI Mechanic?",
                answer = "Add your Gemini API key in Settings, then open a vehicle and select AI Mechanic. Follow the prompts to describe or photograph the problem."
            )

            HelpCard(
                question = "How does Satellite Parking work?",
                answer = "Open a vehicle, allow location access, and mark the parked location. My Ride can then help you view the spot and navigate back to it."
            )

            HelpCard(
                question = "How do backup and restore work?",
                answer = "Open Settings and use Backup to export your vehicle data. Use Restore to import a backup file when needed."
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HelpCard(
    question: String,
    answer: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
