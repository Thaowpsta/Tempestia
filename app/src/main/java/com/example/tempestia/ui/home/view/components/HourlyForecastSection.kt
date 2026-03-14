package com.example.tempestia.ui.home.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.data.weather.model.HourlyWeather
import com.example.tempestia.ui.home.view.formatTemp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.utils.getWeatherEmoji
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HourlyForecastSection(hourlyData: List<HourlyWeather>, isCelsius: Boolean, is24Hour: Boolean) {
    val colors = LocalTempestiaColors.current

    val timeFormatter = SimpleDateFormat(if (is24Hour) "HH:00" else "h a", Locale.getDefault())
    val hourlyData = hourlyData.take(24)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(hourlyData) { hour ->
            val date = Date(hour.dt * 1000L)
            val time = timeFormatter.format(date)
            val icon = getWeatherEmoji(hour.weather.firstOrNull()?.icon ?: "")
            val temp = formatTemp(hour.temp, isCelsius)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.glass)
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = time,
                    color = colors.text2,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = icon, fontSize = 28.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${temp}°",
                    color = colors.text1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
