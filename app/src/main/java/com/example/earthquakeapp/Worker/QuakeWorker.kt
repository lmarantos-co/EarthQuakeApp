package com.example.earthquakeapp.Worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.earthquakeapp.Globals.SettingsManager
import com.example.earthquakeapp.Globals.global_variables
import com.example.earthquakeapp.MainActivity
import com.example.earthquakeapp.R
import com.example.earthquakeapp.Repository.QuakeRepo


class QuakeWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val quakeRepo = QuakeRepo()
            val result = quakeRepo.fetchEarthquakes("all_day")
            val quakes = result.getOrNull() ?: return Result.retry()

            val latestSignificant = quakes.features.firstOrNull {
                it.properties.mag > SettingsManager.pollingMagnitude
            }

            latestSignificant?.let {
                if (isNewQuake(it.id!!)) {
                    showNotification(
                        title = "Earthquake Alert!",
                        message = "Magnitude ${it.properties.mag} in ${it.properties.place}"
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("QuakeWorker", "Failed to fetch earthquakes", e)
            return Result.retry()
        }
    }

    private fun isNewQuake(quakeId: String): Boolean {
        val prefs = applicationContext.getSharedPreferences("quake_prefs", Context.MODE_PRIVATE)
        val lastId = prefs.getString("last_quake_id", null)
        if (lastId == quakeId) return false

        prefs.edit().putString("last_quake_id", quakeId).apply()
        return true
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "quake_alerts"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel if needed (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Earthquake Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for significant earthquakes"
            }
            manager.createNotificationChannel(channel)
        }

        // Optional: Make notification clickable
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
