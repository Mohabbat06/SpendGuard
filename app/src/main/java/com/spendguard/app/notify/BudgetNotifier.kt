package com.spendguard.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.spendguard.app.R
import java.text.NumberFormat
import java.util.Locale

class BudgetNotifier(private val context: Context) {
    fun notifyOverBudget(spent: Double, budget: Double, currency: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.budget_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.budget_channel_desc)
        }
        manager.createNotificationChannel(channel)

        val money = NumberFormat.getNumberInstance(Locale.getDefault())
        val text = "This month: ${currency} ${money.format(spent)} / ${currency} ${money.format(budget)}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Monthly budget exceeded")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val NOTIFICATION_ID = 1001
    }
}
