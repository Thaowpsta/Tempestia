package com.example.tempestia.ui.alerts.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.res.Configuration
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tempestia.BuildConfig
import com.example.tempestia.MainActivity
import com.example.tempestia.R
import com.example.tempestia.repository.WeatherRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

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

            val prefs = applicationContext.getSharedPreferences("TempestiaAlerts", Context.MODE_PRIVATE)

            if (!weather.alerts.isNullOrEmpty()) {
                for (apiAlert in weather.alerts) {
                    val alertId = "${apiAlert.event}_${apiAlert.start}"

                    val alreadyNotified = prefs.getBoolean(alertId, false)

                    if (!alreadyNotified) {
                        val shortDesc = if (apiAlert.description.length > 100) {
                            apiAlert.description.take(100) + "..."
                        } else {
                            apiAlert.description
                        }

                        showNotification(
                            title = applicationContext.getString(R.string.worker_alert_prefix, apiAlert.event),
                            message = shortDesc,
                            type = NotificationType.SOUND
                        )

                        prefs.edit().putBoolean(alertId, true).apply()
                    }
                }
            }

            val activeCustomAlerts = repository.getSubscribedAlerts().firstOrNull()?.filter { it.isActive } ?: emptyList()

            for (alert in activeCustomAlerts) {
                val notifType = NotificationType.valueOf(alert.notificationType)

                if (notifType == NotificationType.ALARM) continue

                when (alert.title) {
                    "Rain Reminder" -> {
                        if (weather.current.weather.any { it.description.contains("rain", true) }) {
                            showNotification(
                                applicationContext.getString(R.string.worker_rain_title),
                                alert.subtitle,
                                notifType
                            )
                        }
                    }

                    "Extreme Heat" -> {
                        if (weather.current.temp >= 40.0) {
                            showNotification(
                                applicationContext.getString(R.string.worker_heat_title),
                                applicationContext.getString(R.string.worker_heat_msg, weather.current.temp.toInt()),
                                notifType
                            )
                        }
                    }

                    "Morning Summary" -> {
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                        val lastSentDay = prefs.getInt("LastMorningSummaryDay", -1)

                        if (currentHour in 7..11 && currentDayOfYear != lastSentDay) {

                            val condition = weather.current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: applicationContext.getString(R.string.condition_clear)

                            showNotification(
                                applicationContext.getString(R.string.worker_morning_title),
                                applicationContext.getString(R.string.worker_morning_msg, weather.current.temp.toInt(), condition),
                                notifType
                            )

                            prefs.edit().putInt("LastMorningSummaryDay", currentDayOfYear).apply()
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
                NotificationType.ALARM -> NotificationManager.IMPORTANCE_NONE
            }
            val channelName = context.getString(R.string.worker_channel_name)
            val channel = NotificationChannel(channelId, channelName, importance)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_TAB", "ALERTS")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    NotificationType.ALARM -> NotificationCompat.PRIORITY_HIGH
                }
            ).setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}