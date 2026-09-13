package com.spendguard.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.spendguard.app.data.db.AppDatabase
import com.spendguard.app.data.firebase.FirebaseSync
import com.spendguard.app.data.prefs.UserPrefs
import com.spendguard.app.data.repo.SpendRepository
import com.spendguard.app.notify.BudgetNotifier

class AppContainer(app: Application) {
    private val db = AppDatabase.create(app)
    val prefs = UserPrefs(app)
    val firebase = FirebaseSync()
    val repository = SpendRepository(
        dao = db.transactionDao(),
        prefs = prefs,
        firebase = firebase,
        notifier = BudgetNotifier(app),
    )
}

class SpendGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val googleAppId = resources.getIdentifier("google_app_id", "string", packageName)
        if (googleAppId != 0) {
            val value = getString(googleAppId)
            if (value.isNotBlank() && !value.contains("REPLACE")) {
                FirebaseApp.initializeApp(this)
            }
        }
        container = AppContainer(this)
    }
}
