package com.spendguard.app.notify

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import com.spendguard.app.SpendGuardApp
import com.spendguard.app.data.parser.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TransactionNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val body = listOf(text, big).firstOrNull { it.isNotBlank() }.orEmpty()
        val parsed = NotificationParser.parse(sbn.packageName, title, "$text $big") ?: return
        val app = application as? SpendGuardApp ?: return
        scope.launch {
            app.container.repository.addFromNotification(
                parsed = parsed,
                rawText = "$title\n$body",
                packageName = sbn.packageName,
                postedAt = sbn.postTime,
            )
        }
    }
}
