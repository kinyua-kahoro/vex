package com.vex.Workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vex.Utils.calculateDelayUntilEvening
import com.vex.Utils.calculateDelayUntilMorning
import java.util.concurrent.TimeUnit

object DailyWorkersScheduler {

    fun schedule(context: Context) {
        val assignmentsWork = PeriodicWorkRequestBuilder<DueAssignmentsWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayUntilMorning(), TimeUnit.MILLISECONDS)
            .build()

        val revisionWork = PeriodicWorkRequestBuilder<RevisionReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateDelayUntilEvening(), TimeUnit.MILLISECONDS)
            .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueue(OneTimeWorkRequestBuilder<DueAssignmentsWorker>().build())

        workManager.enqueueUniquePeriodicWork(
            "due_assignments_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            assignmentsWork
        )

        workManager.enqueueUniquePeriodicWork(
            "revision_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            revisionWork
        )
    }
}
