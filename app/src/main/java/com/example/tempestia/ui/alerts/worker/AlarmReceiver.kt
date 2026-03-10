package com.example.tempestia.ui.alerts.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tempestia.BuildConfig
import com.example.tempestia.MainActivity
import com.example.tempestia.R
import com.example.tempestia.repository.WeatherRepository
import com.example.tempestia.ui.alerts.view.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alertTitle = intent.getStringExtra("ALERT_TITLE") ?: return
        val alertId = intent.getStringExtra("ALERT_ID") ?: "unknown_id"

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = WeatherRepository(context.applicationContext)
                val location = repository.locationFlow.firstOrNull()

                if (location != null) {
                    val response = repository.getWeather(location.first, location.second, BuildConfig.WEATHER_API_KEY)
                    val weather = response.body()

                    var shouldRing = false
                    var message = ""

                    if (weather != null) {
                        when (alertTitle) {
                            "Morning Summary" -> {
                                shouldRing = true
                                val condition = weather.current.weather.firstOrNull()?.description ?: context.getString(R.string.condition_clear)
                                message = context.getString(R.string.alarm_morning_summary_msg, weather.current.temp.toInt(), condition)
                            }
                            "Rain Reminder" -> {
                                if (weather.current.weather.any { it.description.contains("rain", true) }) {
                                    shouldRing = true
                                    message = context.getString(R.string.alarm_rain_msg)
                                }
                            }
                            "Extreme Heat" -> {
                                if (weather.current.temp >= 40.0) {
                                    shouldRing = true
                                    message = context.getString(R.string.alarm_heat_msg, weather.current.temp.toInt())
                                }
                            }
                        }
                    }

                    if (shouldRing) {
                        triggerFullScreenAlarm(context, alertId, alertTitle, message)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun triggerFullScreenAlarm(context: Context, alertId: String, title: String, message: String) {

        val notificationId = alertId.hashCode()
        val wasInForeground = MainActivity.isAppInForeground

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_TITLE", title)
            putExtra("ALARM_MESSAGE", message)
            putExtra("ALERT_ID", alertId)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("WAS_IN_FOREGROUND", wasInForeground)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "tempestia_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.alarm_channel_name)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.tempestia_light)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}