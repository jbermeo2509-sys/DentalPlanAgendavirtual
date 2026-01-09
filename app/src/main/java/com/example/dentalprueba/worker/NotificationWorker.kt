package com.example.dentalprueba.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dentalprueba.R
import com.example.dentalprueba.data.PatientDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = PatientDatabase.getDatabase(applicationContext)
        val patientDao = database.patientDao()
        val patients = patientDao.getAllPatientsSync()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Citas",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val today = Calendar.getInstance()
        // Check for tomorrow
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        
        // Reset to start of day for comparison
        tomorrow.set(Calendar.HOUR_OF_DAY, 0)
        tomorrow.set(Calendar.MINUTE, 0)
        tomorrow.set(Calendar.SECOND, 0)
        tomorrow.set(Calendar.MILLISECOND, 0)

        for (patient in patients) {
            if (patient.isActive) {
                val nextDate = patient.getNextAppointmentDate()
                val nextCal = Calendar.getInstance()
                nextCal.time = nextDate
                
                nextCal.set(Calendar.HOUR_OF_DAY, 0)
                nextCal.set(Calendar.MINUTE, 0)
                nextCal.set(Calendar.SECOND, 0)
                nextCal.set(Calendar.MILLISECOND, 0)

                // If the appointment is tomorrow
                if (nextCal.timeInMillis == tomorrow.timeInMillis) {
                    sendNotification(
                        notificationManager,
                        patient.id.hashCode(),
                        "Cita Mañana",
                        "${patient.firstName} ${patient.lastName} tiene cita mañana para ${patient.procedure}"
                    )
                }
            }
        }

        return Result.success()
    }

    private fun sendNotification(
        manager: NotificationManager,
        id: Int,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda) // Using a default android icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "appointment_reminders"
    }
}
