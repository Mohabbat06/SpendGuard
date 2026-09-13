package com.spendguard.app.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spendguard.app.data.db.TransactionEntity
import com.spendguard.app.data.prefs.UserSettings
import kotlinx.coroutines.tasks.await

class FirebaseSync {
    val isConfigured: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (_: IllegalStateException) {
            false
        }

    fun currentUid(): String? {
        if (!isConfigured) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    suspend fun signIn(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        if (isConfigured) FirebaseAuth.getInstance().signOut()
    }

    suspend fun pushTransaction(entity: TransactionEntity) {
        val uid = currentUid() ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("transactions")
            .document(entity.id)
            .set(
                mapOf(
                    "amount" to entity.amount,
                    "merchant" to entity.merchant,
                    "occurredAt" to entity.occurredAt,
                    "source" to entity.source,
                    "rawText" to entity.rawText,
                    "monthKey" to entity.monthKey,
                ),
            )
            .await()
    }

    suspend fun pushSettings(settings: UserSettings) {
        val uid = currentUid() ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(
                mapOf(
                    "monthlyBudget" to settings.monthlyBudget,
                    "currency" to settings.currency,
                    "alarmEnabled" to settings.alarmEnabled,
                    "updatedAt" to System.currentTimeMillis(),
                ),
            )
            .await()
    }
}
