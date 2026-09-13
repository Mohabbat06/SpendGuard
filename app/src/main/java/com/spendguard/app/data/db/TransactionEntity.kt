package com.spendguard.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["dedupeKey"], unique = true)],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val merchant: String,
    val occurredAt: Long,
    val source: String,
    val rawText: String,
    val monthKey: String,
    val dedupeKey: String,
    val synced: Boolean = false,
)
