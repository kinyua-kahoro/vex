package com.vex.Workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vex.Utils.sendNotification
import com.vex.data.repository.AssignmentsRepository

class DueAssignmentsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dueAssignments = AssignmentsRepository.getDueAssignmentsWithin7Days()

        val message = if (dueAssignments.isEmpty()) {
            "No assignments due soon."
        } else {
            "Assignments due: " + dueAssignments.joinToString { it.title }
        }

        applicationContext.sendNotification(
            id = 1001,
            channelId = "due_assignments_channel",
            title = "Assignments Reminder",
            message = message
        )
        return Result.success()
    }
}