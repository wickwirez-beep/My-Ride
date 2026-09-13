package com.wickwirez.myride.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wickwirez.myride.data.PromoCodeChecker
import kotlinx.coroutines.launch

@Composable
fun PaywallScreen(
    onPurchaseClick: () -> Unit,
    onPromoRedeemed: () -> Unit
) {
    var promoCode by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your Free Trial Has Ended",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Unlock My Ride for life with a one-time purchase — no subscriptions, no ads, ever.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlock for \$2.99")
            }

            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Have a promo code?",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = {
                            promoCode = it
                            errorMessage = null
                        },
                        label = { Text("Enter code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (promoCode.isBlank()) return@OutlinedButton
                            isChecking = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = PromoCodeChecker.redeem(promoCode)
                                isChecking = false
                                result.onSuccess { isValid ->
                                    if (isValid) {
                                        onPromoRedeemed()
                                    } else {
                                        errorMessage = "Invalid code. Please check and try again."
                                    }
                                }.onFailure {
                                    errorMessage = "Couldn't check code. Check your connection and try again."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        } else {
                            Text("Redeem Code")
                        }
                    }
                }
            }
        }
    }
}
