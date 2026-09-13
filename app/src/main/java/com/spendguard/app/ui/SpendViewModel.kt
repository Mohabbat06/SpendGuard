package com.spendguard.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendguard.app.SpendGuardApp
import com.spendguard.app.data.db.TransactionEntity
import com.spendguard.app.data.prefs.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UiState(
    val settings: UserSettings = UserSettings(),
    val monthTotal: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val listenerEnabled: Boolean = false,
    val authMessage: String? = null,
    val busy: Boolean = false,
)

class SpendViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SpendGuardApp
    private val repo = app.container.repository
    private val firebase = app.container.firebase

    private val listener = MutableStateFlow(false)
    private val authMessage = MutableStateFlow<String?>(null)
    private val busy = MutableStateFlow(false)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    val firebaseConfigured: Boolean get() = firebase.isConfigured
    val signedInEmail: String?
        get() = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
        } catch (_: Exception) {
            null
        }

    init {
        viewModelScope.launch {
            combine(
                repo.settings,
                repo.monthTotal(),
                repo.monthTransactions(),
                listener,
                combine(authMessage, busy) { msg, isBusy -> msg to isBusy },
            ) { settings, total, txns, enabled, auth ->
                UiState(
                    settings = settings,
                    monthTotal = total,
                    transactions = txns,
                    listenerEnabled = enabled,
                    authMessage = auth.first,
                    busy = auth.second,
                )
            }.collect { _state.value = it }
        }
    }

    fun refreshListenerEnabled(enabled: Boolean) {
        listener.value = enabled
    }

    fun completeOnboarding() {
        viewModelScope.launch { repo.completeOnboarding() }
    }

    fun addManual(amount: Double, merchant: String) {
        viewModelScope.launch { repo.addManual(amount, merchant) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun saveBudget(amount: Double) {
        viewModelScope.launch { repo.setBudget(amount) }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch { repo.setCurrency(code) }
    }

    fun setAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setAlarmEnabled(enabled) }
    }

    fun signIn(email: String, password: String) = authAction {
        firebase.signIn(email, password)
        "Signed in"
    }

    fun signUp(email: String, password: String) = authAction {
        firebase.signUp(email, password)
        "Account created"
    }

    fun signOut() {
        firebase.signOut()
        authMessage.value = "Signed out"
    }

    private fun authAction(block: suspend () -> String) {
        viewModelScope.launch {
            busy.value = true
            authMessage.value = runCatching { block() }.getOrElse { it.message ?: "Auth failed" }
            busy.value = false
        }
    }
}
