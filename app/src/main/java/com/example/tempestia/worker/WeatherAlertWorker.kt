package com.example.tempestia.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.res.Configuration
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tempestia.BuildConfig
import com.example.tempestia.R
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.flow.firstOrNull

class WeatherAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val repository = WeatherRepository(applicationContext)

            val location = repository.locationFlow.firstOrNull() ?: return Result.success()

            val response = repository.getWeather(location.first, location.second, BuildConfig.WEATHER_API_KEY)
            val weather = response.body() ?: return Result.success()

            val activeAlerts = repository.getSubscribedAlerts().firstOrNull()?.filter { it.isActive } ?: emptyList()

            for (alert in activeAlerts) {
                val notifType = NotificationType.valueOf(alert.notificationType)

                when (alert.title) {
                    "Rain Reminder" -> {
                        if (weather.current.weather.any { it.description.contains("rain", true) }) {
                            showNotification("Rain Expected! 🌧️", alert.subtitle, notifType)
                        }
                    }

                    "Severe Thunderstorm" -> {
                        if (weather.current.weather.any { it.description.contains("thunderstorm", true) }) {
                            showNotification("Severe Thunderstorm ⛈️", alert.subtitle, notifType)
                        }
                    }

                    "Extreme Heat" -> {
                        if (weather.current.temp >= 40.0) {
                            showNotification("Extreme Heat Warning 🌡️", "It is currently ${weather.current.temp.toInt()}°C.", notifType)
                        }
                    }

                    "Morning Summary" -> {
                        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                        if (currentHour == 7) {
                            val condition = weather.current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Clear"
                            showNotification(
                                "Good Morning! 🌅",
                                "It's ${weather.current.temp.toInt()}°C and $condition today.",
                                notifType
                            )
                        }
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun showNotification(title: String, message: String, type: NotificationType) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tempestia_alerts_${type.name}"

        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val icon = if (isDarkMode) R.drawable.tempestia_dark else R.drawable.tempestia_light

        // Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = when (type) {
                NotificationType.SILENT -> NotificationManager.IMPORTANCE_LOW     // No sound, no popup
                NotificationType.PUSH -> NotificationManager.IMPORTANCE_DEFAULT   // Standard popup
                NotificationType.SOUND -> NotificationManager.IMPORTANCE_HIGH     // Popup + Sound
            }
            val channel = NotificationChannel(channelId, "Weather Alerts", importance)
            manager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, com.example.tempestia.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_TAB", "ALERTS")
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                when (type) {
                    NotificationType.SILENT -> NotificationCompat.PRIORITY_LOW
                    NotificationType.PUSH -> NotificationCompat.PRIORITY_DEFAULT
                    NotificationType.SOUND -> NotificationCompat.PRIORITY_HIGH
                }
            ).setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}