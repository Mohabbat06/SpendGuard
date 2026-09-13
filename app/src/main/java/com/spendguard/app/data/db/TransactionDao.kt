package com.spendguard.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE monthKey = :monthKey ORDER BY occurredAt DESC")
    fun observeMonth(monthKey: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE monthKey = :monthKey")
    fun observeMonthTotal(monthKey: String): Flow<Double>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM transactions WHERE synced = 0")
    suspend fun unsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
