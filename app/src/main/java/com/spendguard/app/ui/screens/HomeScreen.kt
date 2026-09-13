package com.spendguard.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spendguard.app.data.db.TransactionEntity
import com.spendguard.app.ui.UiState
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: UiState,
    onAddManual: (Double, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    val budget = state.settings.monthlyBudget
    val ratio = if (budget <= 0) 0f else (state.monthTotal / budget).toFloat().coerceIn(0f, 1f)
    val over = budget > 0 && state.monthTotal > budget
    val money = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("This month", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${state.settings.currency} ${money.format(state.monthTotal)}",
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (budget > 0) {
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                    Text("Budget ${state.settings.currency} ${money.format(budget)}")
                    if (over) {
                        Text(
                            "You are over this month’s limit.",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    Text("Set a monthly budget in Settings to get an alarm.")
                }
                if (!state.listenerEnabled) {
                    Text("Payment capture is off. Turn it on in Settings so amounts are added automatically.")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAdd = true }) { Text("Add expense") }
        }
        Text("Transactions", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.transactions, key = { it.id }) { txn ->
                TransactionRow(txn, state.settings.currency, money, onDelete)
            }
        }
    }

    if (showAdd) {
        AddExpenseDialog(
            onDismiss = { showAdd = false },
            onSave = { amount, merchant ->
                onAddManual(amount, merchant)
                showAdd = false
            },
        )
    }
}

@Composable
private fun TransactionRow(
    txn: TransactionEntity,
    currency: String,
    money: NumberFormat,
    onDelete: (String) -> Unit,
) {
    val whenText = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(txn.occurredAt))
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.merchant, style = MaterialTheme.typography.titleMedium)
                Text("$whenText · ${txn.source}")
            }
            Column {
                Text("$currency ${money.format(txn.amount)}")
                TextButton(onClick = { onDelete(txn.id) }) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("What was it for?") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amount.replace(",", "").toDoubleOrNull()
                    if (value != null && value > 0) onSave(value, merchant)
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
