package com.example.dentalprueba

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dentalprueba.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class DentalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Schedule the daily check for notifications
        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyNotificationCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }
}
