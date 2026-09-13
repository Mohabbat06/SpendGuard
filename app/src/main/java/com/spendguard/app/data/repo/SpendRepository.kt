package com.spendguard.app.data.repo

import com.spendguard.app.data.db.TransactionDao
import com.spendguard.app.data.db.TransactionEntity
import com.spendguard.app.data.firebase.FirebaseSync
import com.spendguard.app.data.parser.ParsedTransaction
import com.spendguard.app.data.prefs.UserPrefs
import com.spendguard.app.data.prefs.UserSettings
import com.spendguard.app.notify.BudgetNotifier
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SpendRepository(
    private val dao: TransactionDao,
    private val prefs: UserPrefs,
    private val firebase: FirebaseSync,
    private val notifier: BudgetNotifier,
) {
    val settings: Flow<UserSettings> = prefs.settings

    fun monthTransactions(monthKey: String = currentMonthKey()): Flow<List<TransactionEntity>> {
        return dao.observeMonth(monthKey)
    }

    fun monthTotal(monthKey: String = currentMonthKey()): Flow<Double> {
        return dao.observeMonthTotal(monthKey)
    }

    suspend fun addFromNotification(
        parsed: ParsedTransaction,
        rawText: String,
        packageName: String,
        postedAt: Long,
    ) {
        val dedupe = "$packageName|$postedAt|${parsed.amount}|$rawText".hashCode().toString()
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            amount = parsed.amount,
            merchant = parsed.merchant ?: packageName.substringAfterLast('.'),
            occurredAt = postedAt,
            source = "notification",
            rawText = rawText.take(400),
            monthKey = monthKeyFor(postedAt),
            dedupeKey = dedupe,
        )
        val inserted = dao.insert(entity)
        if (inserted == -1L) return
        maybeAlarm()
        runCatching { firebase.pushTransaction(entity); dao.markSynced(entity.id) }
    }

    suspend fun addManual(amount: Double, merchant: String) {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            amount = amount,
            merchant = merchant.ifBlank { "Manual" },
            occurredAt = now,
            source = "manual",
            rawText = "",
            monthKey = monthKeyFor(now),
            dedupeKey = "manual-${UUID.randomUUID()}",
        )
        dao.insert(entity)
        maybeAlarm()
        runCatching { firebase.pushTransaction(entity); dao.markSynced(entity.id) }
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun setBudget(amount: Double) {
        prefs.setMonthlyBudget(amount)
        val settings = prefs.settings.first()
        runCatching { firebase.pushSettings(settings.copy(monthlyBudget = amount)) }
        maybeAlarm()
    }

    suspend fun setCurrency(code: String) {
        prefs.setCurrency(code)
        runCatching { firebase.pushSettings(prefs.settings.first()) }
    }

    suspend fun setAlarmEnabled(enabled: Boolean) {
        prefs.setAlarmEnabled(enabled)
        runCatching { firebase.pushSettings(prefs.settings.first()) }
    }

    suspend fun completeOnboarding() {
        prefs.setOnboardingDone()
    }

    private suspend fun maybeAlarm() {
        val settings = prefs.settings.first()
        if (!settings.alarmEnabled || settings.monthlyBudget <= 0) return
        val month = currentMonthKey()
        val total = dao.observeMonthTotal(month).first()
        if (total <= settings.monthlyBudget) return
        if (settings.lastAlarmMonth == month) return
        notifier.notifyOverBudget(
            spent = total,
            budget = settings.monthlyBudget,
            currency = settings.currency,
        )
        prefs.setLastAlarmMonth(month)
    }

    companion object {
        fun currentMonthKey(zone: ZoneId = ZoneId.systemDefault()): String {
            return YearMonth.now(zone).toString()
        }

        fun monthKeyFor(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String {
            return YearMonth.from(Instant.ofEpochMilli(epochMillis).atZone(zone)).toString()
        }
    }
}
