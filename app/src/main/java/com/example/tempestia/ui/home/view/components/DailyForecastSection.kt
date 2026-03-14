package com.example.tempestia.ui.home.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.data.weather.model.DailyWeather
import com.example.tempestia.ui.home.view.formatTemp
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.utils.getWeatherEmoji
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyForecastSection(dailyData: List<DailyWeather>, isCelsius: Boolean) {
    val colors = LocalTempestiaColors.current
    val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())

    val dailyData = dailyData.drop(1).take(7)
    val weeklyMin = dailyData.minOfOrNull { it.temp.min }?.toFloat() ?: 0f
    val weeklyMax = dailyData.maxOfOrNull { it.temp.max }?.toFloat() ?: 100f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        dailyData.forEachIndexed { index, day ->
            val date = Date(day.dt * 1000L)
            val dayName = dayFormatter.format(date)
            val icon = getWeatherEmoji(day.weather.firstOrNull()?.icon ?: "")

            val maxTemp = formatTemp(day.temp.max, isCelsius)
            val minTemp = formatTemp(day.temp.min, isCelsius)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    dayName,
                    color = colors.text2,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    icon,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center
                )
                TemperatureRangeBar(
                    minTemp = day.temp.min.toFloat(),
                    maxTemp = day.temp.max.toFloat(),
                    weeklyMin = weeklyMin,
                    weeklyMax = weeklyMax,
                    modifier = Modifier
                        .weight(1.5f)
                        .padding(horizontal = 12.dp)
                )
                Text(
                    "${maxTemp}° / ${minTemp}°",
                    color = colors.text1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            if (index < dailyData.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colors.glassBorder.copy(alpha = 0.3f)
                )
            }
        }
    }
}
