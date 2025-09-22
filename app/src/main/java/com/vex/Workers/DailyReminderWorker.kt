package com.vex.Workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vex.R
import com.vex.data.repository.AssignmentsRepository
import com.vex.model.DueAssignment
import com.vex.ui.theme.screens.Home.HomeViewModel
import kotlin.collections.joinToString

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 🔹 1. Query Firebase for assignments due within 7 days & not done
        val dueAssignments = fetchDueAssignments()

        // 🔹 2. Build notification text
        val message = if (dueAssignments.isEmpty()) {
            "No assignments due soon. Check topics to revise!"
        } else {
            "Assignments due: " + dueAssignments.joinToString { it.title }
        }

        // 🔹 3. Send notification
        sendNotification("Study Reminder", message)

        return Result.success()
    }

    private suspend fun fetchDueAssignments(): List<DueAssignment> {
        return AssignmentsRepository.getDueAssignmentsWithin7Days()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "daily_reminder_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create channel once (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // your icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(1001, notification)
    }
}
