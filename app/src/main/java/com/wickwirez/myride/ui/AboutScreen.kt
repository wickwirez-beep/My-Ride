package com.wickwirez.myride.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.BuildConfig
import com.wickwirez.myride.R

// TODO: replace with your real Ko-fi / Buy Me a Coffee / PayPal.me link
private const val DONATE_URL = "https://ko-fi.com/harlancowan"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.flag_wrenches_badge),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
            Text("My Ride", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            Text(
                "Complete vehicle maintenance and expense tracker.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "100% Ad-Free \u2014 No Ads, Ever.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Vehicle photos, VIN decode and scanning, maintenance reminders, " +
                    "service history, spending charts, backup & restore, printing, " +
                    "Digital Glove Box document storage, AI Mechanic diagnostics, " +
                    "Satellite Parking, and an AI assistant powered by Google Gemini.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Created and built by Wick",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Built with Termux, GitHub Actions, and Kotlin/Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { showSupportDialog = true }) {
                Text("\u2764\uFE0F Support My Ride")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("wickwirez@yahoo.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "My Ride Feedback")
                }
                context.startActivity(Intent.createChooser(intent, "Send Feedback"))
            }) {
                Text("Send Feedback")
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Support My Ride") },
            text = {
                Text(
                    "Hi, I'm Wick \u2014 I built My Ride solo, from my phone, with no ad revenue " +
                        "funding it. If the app's been useful for tracking your vehicle, a small " +
                        "tip helps keep it going and helps me keep shipping new features."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL))
                    context.startActivity(intent)
                    showSupportDialog = false
                }) {
                    Text("\u2764\uFE0F Thank You! \u2014 \$1.99")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}
