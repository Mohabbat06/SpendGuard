package com.spendguard.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.spendguard.app.ui.UiState

private val currencies = listOf("BDT", "INR", "USD", "EUR", "PHP", "SGD")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: UiState,
    firebaseReady: Boolean,
    signedInEmail: String?,
    onSaveBudget: (Double) -> Unit,
    onCurrency: (String) -> Unit,
    onAlarm: (Boolean) -> Unit,
    onOpenCapture: () -> Unit,
    onRefreshCapture: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignOut: () -> Unit,
) {
    var budgetText by remember(state.settings.monthlyBudget) {
        mutableStateOf(
            if (state.settings.monthlyBudget == 0.0) "" else state.settings.monthlyBudget.toString(),
        )
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Budget", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            label = { Text("Monthly limit") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                budgetText.replace(",", "").toDoubleOrNull()?.let(onSaveBudget)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save budget") }

        Text("Currency")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            currencies.forEach { code ->
                FilterChip(
                    selected = state.settings.currency == code,
                    onClick = { onCurrency(code) },
                    label = { Text(code) },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Alarm when I go over")
                Text("You’ll get a phone notification the first time this month’s spend passes the limit.")
            }
            Switch(checked = state.settings.alarmEnabled, onCheckedChange = onAlarm)
        }

        Text("Automatic capture", style = MaterialTheme.typography.titleLarge)
        Text(
            if (state.listenerEnabled) {
                "SpendGuard can read payment notifications on this phone."
            } else {
                "Capture is off. Enable SpendGuard in Android notification access."
            },
        )
        OutlinedButton(onClick = onOpenCapture, modifier = Modifier.fillMaxWidth()) {
            Text("Open notification access")
        }
        OutlinedButton(onClick = onRefreshCapture, modifier = Modifier.fillMaxWidth()) {
            Text("I’ve turned it on — refresh")
        }

        Text("Cloud backup (optional)", style = MaterialTheme.typography.titleLarge)
        if (!firebaseReady) {
            Text("Add google-services.json from Firebase to enable sign-in and backup. The app works on this phone without it.")
        } else if (signedInEmail != null) {
            Text("Signed in as $signedInEmail")
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        } else {
            Text("Sign in to keep a copy of your totals in Firebase (same owner account).")
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSignIn(email, password) }, enabled = !state.busy) { Text("Sign in") }
                OutlinedButton(onClick = { onSignUp(email, password) }, enabled = !state.busy) { Text("Create account") }
            }
            state.authMessage?.let { Text(it) }
        }
    }
}
