package com.example.earthquakeapp.Worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.earthquakeapp.Globals.SettingsManager
import com.example.earthquakeapp.Globals.global_variables
import com.example.earthquakeapp.R
import com.example.earthquakeapp.Repository.QuakeRepo

class QuakeWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val quakeRepo = QuakeRepo()
            val result = quakeRepo.fetchEarthquakes("all_day") // or "hour"

            val latestSignificant = result.getOrNull()!!.features.firstOrNull {
                it.properties.mag > SettingsManager.pollingMagnitude
            }

            latestSignificant?.let {
                showNotification(
                    title = "Earthquake Alert!",
                    message = "Magnitude ${it.properties.mag} in ${it.properties.place}"
                )
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "quake_alerts"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Earthquake Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1001, notification)
    }
}
