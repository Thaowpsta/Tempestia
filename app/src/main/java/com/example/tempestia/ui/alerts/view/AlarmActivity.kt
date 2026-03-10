package com.example.tempestia.ui.alerts.view

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.R
import com.example.tempestia.theme.TempestiaTheme
import com.example.tempestia.ui.alerts.worker.AlarmScheduler
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.DarkTempestiaColors
import com.example.tempestia.ui.onboarding.view.LightTempestiaColors
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors

class AlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wasInForeground = false
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        super.onCreate(savedInstanceState)

        wasInForeground = intent.getBooleanExtra("WAS_IN_FOREGROUND", false)

        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
        if (notificationId != -1) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(notificationId)
        }

        val title = intent.getStringExtra("ALARM_TITLE") ?: getString(R.string.weather_alarm_default)
        val message = intent.getStringExtra("ALARM_MESSAGE") ?: getString(R.string.time_to_wake_up)
        val alertId = intent.getStringExtra("ALERT_ID") ?: "unknown_id"

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val ringerMode = audioManager.ringerMode

        val alarmAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        if (ringerMode == android.media.AudioManager.RINGER_MODE_NORMAL) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone?.audioAttributes = alarmAttributes
            }
            ringtone?.play()
        }

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(effect, alarmAttributes)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0, alarmAttributes)
        }

        setContent {
            TempestiaTheme {
                val isSystemDark = isSystemInDarkTheme()
                val colors = if (isSystemDark) DarkTempestiaColors else LightTempestiaColors

                CompositionLocalProvider(LocalTempestiaColors provides colors) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgDeep)) {

                        AnimatedParticleBackground()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("⏰", fontSize = 80.sp)
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                title,
                                color = colors.text1,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                message,
                                color = colors.text2,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(64.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        stopAlarm()
                                        AlarmScheduler.snoozeAlarm(
                                            applicationContext,
                                            alertId,
                                            title,
                                            2
                                        )
                                        android.widget.Toast.makeText(
                                            applicationContext,
                                            getString(R.string.snoozed_msg),
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()

                                        navigateToHome()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.purpleCore),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.snooze_btn),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        stopAlarm()

                                        navigateToHome()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFFFF4B4B
                                        )
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.dismiss_btn),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        stopAlarm()

        if (wasInForeground) {
            val intent = android.content.Intent(this, com.example.tempestia.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_TAB", "HOME")
            }
            startActivity(intent)
        }

        finish()
    }

    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}