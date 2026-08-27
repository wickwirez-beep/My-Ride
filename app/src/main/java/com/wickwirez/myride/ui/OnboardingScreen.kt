package com.wickwirez.myride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.DirectionsCar,
        title = "Welcome to My Ride",
        description = "Track every vehicle you own — service history, mileage, and more, all in one place."
    ),
    OnboardingPage(
        icon = Icons.Filled.Description,
        title = "Digital Glove Box",
        description = "Store registration, insurance, and other documents right in the app, organized and easy to find."
    ),
    OnboardingPage(
        icon = Icons.Filled.Build,
        title = "AI Mechanic",
        description = "Snap a photo of an issue and get an instant AI diagnosis — no guessing what that noise means."
    ),
    OnboardingPage(
        icon = Icons.Filled.LocalParking,
        title = "Satellite Parking",
        description = "Mark where you parked, then get satellite view, street view, or walking directions straight back to it."
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(onboardingPages.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (selected) 10.dp else 8.dp)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onFinished,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }
                    Button(
                        onClick = {
                            if (pagerState.currentPage == onboardingPages.lastIndex) {
                                onFinished()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (pagerState.currentPage == onboardingPages.lastIndex) "Get Started" else "Next")
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val item = onboardingPages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
