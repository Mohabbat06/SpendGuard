package com.spendguard.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSettings(
    val monthlyBudget: Double = 0.0,
    val currency: String = "BDT",
    val alarmEnabled: Boolean = true,
    val onboardingDone: Boolean = false,
    val lastAlarmMonth: String = "",
)

class UserPrefs(context: Context) {
    private val store = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("spendguard_prefs")
    }

    val settings: Flow<UserSettings> = store.data.map { prefs ->
        UserSettings(
            monthlyBudget = prefs[MONTHLY_BUDGET] ?: 0.0,
            currency = prefs[CURRENCY] ?: "BDT",
            alarmEnabled = prefs[ALARM_ENABLED] ?: true,
            onboardingDone = prefs[ONBOARDING_DONE] ?: false,
            lastAlarmMonth = prefs[LAST_ALARM_MONTH] ?: "",
        )
    }

    suspend fun setMonthlyBudget(value: Double) {
        store.edit { it[MONTHLY_BUDGET] = value }
    }

    suspend fun setCurrency(value: String) {
        store.edit { it[CURRENCY] = value }
    }

    suspend fun setAlarmEnabled(value: Boolean) {
        store.edit { it[ALARM_ENABLED] = value }
    }

    suspend fun setOnboardingDone() {
        store.edit { it[ONBOARDING_DONE] = true }
    }

    suspend fun setLastAlarmMonth(monthKey: String) {
        store.edit { it[LAST_ALARM_MONTH] = monthKey }
    }

    private companion object {
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val CURRENCY = stringPreferencesKey("currency")
        val ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LAST_ALARM_MONTH = stringPreferencesKey("last_alarm_month")
    }
}
