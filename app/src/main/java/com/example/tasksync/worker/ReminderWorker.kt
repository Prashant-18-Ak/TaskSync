package com.example.tasksync.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tasksync.R

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val title = inputData.getString("title") ?: "Task Reminder"

        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val channelId = "task_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Task Due")
            .setContentText("$title is due now!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title is due now! Open TaskSync to view details.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )

        return Result.success()
    }
}