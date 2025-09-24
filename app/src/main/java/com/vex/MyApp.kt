package com.vex

import android.app.Application
import androidx.work.Configuration
import android.util.Log
import com.vex.Workers.DailyWorkersScheduler

class Vex : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        DailyWorkersScheduler.schedule(this)
    }
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}