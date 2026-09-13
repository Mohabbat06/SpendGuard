package com.spendguard.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    captureEnabled: Boolean,
    onOpenCaptureSettings: () -> Unit,
    onContinue: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("SpendGuard", style = MaterialTheme.typography.headlineLarge)
            Text(
                "This app stays on your phone. When you pay with bKash, Nagad, a bank app, or UPI, SpendGuard reads that payment notification and adds the amount for you.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "Turn on notification access for SpendGuard so it can see payment alerts. It does not read other people’s phones, and it does not open your bank apps.",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                onClick = onOpenCaptureSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (captureEnabled) "Capture is on" else "Enable payment capture")
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}
