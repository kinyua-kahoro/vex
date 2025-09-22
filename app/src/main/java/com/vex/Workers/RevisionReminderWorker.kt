package com.vex.Workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vex.Utils.sendNotification

class RevisionReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        applicationContext.sendNotification(
            id = 1002,
            channelId = "revision_channel",
            title = "Revision Reminder",
            message = "Don’t forget to review your topics today!"
        )
        return Result.success()
    }
}