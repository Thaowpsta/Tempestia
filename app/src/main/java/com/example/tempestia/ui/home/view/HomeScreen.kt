package com.example.tempestia.ui.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tempestia.R
import com.example.tempestia.data.weather.model.DailyWeather
import com.example.tempestia.data.weather.model.HourlyWeather
import com.example.tempestia.data.weather.model.WeatherResponse
import com.example.tempestia.ui.home.viewModel.WeatherState
import com.example.tempestia.ui.home.viewModel.WeatherViewModel
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.utils.getWeatherEmoji
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun formatTemp(tempCelsius: Double, isCelsius: Boolean): Int {
    return if (isCelsius) tempCelsius.roundToInt() else ((tempCelsius * 9 / 5) + 32).roundToInt()
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel,
    onboardingViewModel: OnboardingViewModel
) {
    val colors = LocalTempestiaColors.current
    val weatherState by weatherViewModel.weatherState.collectAsState()

    val userLocation by onboardingViewModel.locationFlow.collectAsState(initial = null)

    val isCelsius by weatherViewModel.isCelsiusFlow.collectAsState(initial = true)
    val is24Hour by weatherViewModel.is24HourFlow.collectAsState(initial = false)

    val activeLanguage by weatherViewModel.languageFlow.collectAsState(initial = "en")

    LaunchedEffect(userLocation, activeLanguage) {
        userLocation?.let { (lat, lng) ->
            weatherViewModel.getWeather(lat, lng)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {

        AnimatedParticleBackground()

        AnimatedContent(
            targetState = weatherState,
            transitionSpec = {
                fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "WeatherStateTransition"
        ) { state ->
            when (state) {
                is WeatherState.Idle, is WeatherState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.purpleBright)
                    }
                }

                is WeatherState.Success -> {
                    WeatherDashboard(
                        data = state.weatherData,
                        cityName = state.cityName,
                        isCelsius = isCelsius,
                        is24Hour = is24Hour,
                        onSwipeRefresh = { weatherViewModel.refreshWeather(isSwipeRefresh = true) }
                    )
                }

                is WeatherState.Error -> {
                    ErrorScreen(state.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboard(
    data: WeatherResponse,
    cityName: String,
    isCelsius: Boolean,
    is24Hour: Boolean,
    onSwipeRefresh: () -> Unit
) {
    val colors = LocalTempestiaColors.current
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                onSwipeRefresh()
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize(),
        state = pullRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullRefreshState,
                containerColor = colors.bgDeep, // Styled to match your app
                color = colors.purpleBright
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(colors.glass, RoundedCornerShape(50.dp))
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = colors.purpleBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cityName,
                    color = colors.text1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val mainIconCode = data.current.weather.firstOrNull()?.icon ?: ""
            Text(getWeatherEmoji(mainIconCode), fontSize = 64.sp)

            Spacer(modifier = Modifier.height(14.dp))

            val mainTemp = formatTemp(data.current.temp, isCelsius)
            Text(
                text = buildAnnotatedString {
                    append("$mainTemp")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 32.sp,
                            baselineShift = BaselineShift.Superscript
                        )
                    ) {
                        append("°")
                    }
                },
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(colors.text1, colors.purpleBright)),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Light,
                ),
                modifier = Modifier.offset(x = 10.dp)
            )

            val condition = data.current.weather.firstOrNull()?.description ?: stringResource(R.string.unknown)
            Text(
                text = condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.glass, RoundedCornerShape(32.dp))
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
                    .padding(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val windSpeedKmh = (data.current.windSpeed * 3.6).toInt()
                    val feelsLikeTemp = formatTemp(data.current.feelsLike, isCelsius)

                    MetricItem(stringResource(R.string.feels_like), "${feelsLikeTemp}°", Modifier.weight(1f))
                    MetricItem(stringResource(R.string.humidity), "${data.current.humidity}%", Modifier.weight(1f))
                    MetricItem(stringResource(R.string.wind), "$windSpeedKmh km/h", Modifier.weight(1f))
                    MetricItem(stringResource(R.string.uv), "${data.current.uvi.roundToInt()}", Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = colors.text3,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(y = (-1).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hourly_forecast),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }

            HourlyForecastSection(data.hourly, isCelsius, is24Hour)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = colors.text3,
                    modifier = Modifier
                        .size(16.dp)
                        .offset(y = (-1).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.six_day_forecast),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }

            DailyForecastSection(data.daily, isCelsius)

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.atmospheric_details),
                    color = colors.text3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                val humiditySubtext = when {
                    data.current.humidity < 30 -> stringResource(R.string.dry_air)
                    data.current.humidity < 60 -> stringResource(R.string.comfortable)
                    data.current.humidity < 80 -> stringResource(R.string.slightly_humid)
                    else -> stringResource(R.string.very_humid)
                }

                val windSpeed = data.current.windSpeed.toInt()
                val windSubtext = when {
                    windSpeed < 2 -> stringResource(R.string.calm_conditions)
                    windSpeed < 5 -> stringResource(R.string.light_breeze)
                    windSpeed < 10 -> stringResource(R.string.moderate_breeze)
                    windSpeed < 15 -> stringResource(R.string.strong_wind)
                    else -> stringResource(R.string.gale_force)
                }

                val visibilityKm = data.current.visibility / 1000
                val visSubtext = if (visibilityKm >= 10) stringResource(R.string.perfect_clear_view) else stringResource(R.string.reduced_visibility)

                val pressure = data.current.pressure
                val pressSubtext = when {
                    pressure > 1015 -> stringResource(R.string.high_pressure)
                    pressure < 1005 -> stringResource(R.string.low_pressure)
                    else -> stringResource(R.string.normal_pressure)
                }

                val timeFormatter = SimpleDateFormat(if (is24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
                val sunriseTime = timeFormatter.format(Date(data.current.sunrise * 1000L))
                val sunsetTime = timeFormatter.format(Date(data.current.sunset * 1000L))

                data class GridItem(val title: String, val value: String, val subtext: String, val icon: String)

                val gridItems = listOf(
                    GridItem(stringResource(R.string.humidity), "${data.current.humidity}%", humiditySubtext, "💧"),
                    GridItem(stringResource(R.string.wind), "$windSpeed m/s", windSubtext, "💨"),
                    GridItem(stringResource(R.string.visibility), "$visibilityKm km", visSubtext, "👁️"),
                    GridItem(stringResource(R.string.pressure), "$pressure hPa", pressSubtext, "🌡️"),
                    GridItem(stringResource(R.string.sunrise), sunriseTime, stringResource(R.string.morning_light), "🌅"),
                    GridItem(stringResource(R.string.sunset), sunsetTime, stringResource(R.string.evening_dusk), "🌇")
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    gridItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                AtmosphericCard(
                                    title = item.title,
                                    value = item.value,
                                    subtext = item.subtext,
                                    icon = item.icon,
                                    modifier = Modifier.weight(1f)  // Ensures they take exactly 50% width
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

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

@Composable
fun TemperatureRangeBar(
    minTemp: Float,
    maxTemp: Float,
    weeklyMin: Float,
    weeklyMax: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalTempestiaColors.current

    Canvas(modifier = modifier.height(4.dp).fillMaxWidth()) {
        val range = weeklyMax - weeklyMin
        val safeRange = if (range == 0f) 1f else range

        val startX = size.width * ((minTemp - weeklyMin) / safeRange)
        val endX = size.width * ((maxTemp - weeklyMin) / safeRange)

        drawLine(
            color = colors.glassBorder,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(colors.purpleCore, colors.goldLight),
                startX = 0f,
                endX = size.width
            ),
            start = Offset(startX, size.height / 2),
            end = Offset(endX, size.height / 2),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AtmosphericCard(
    title: String,
    value: String,
    subtext: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalTempestiaColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.glass)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = icon, fontSize = 28.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            color = colors.text3,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = colors.text1,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtext,
            color = colors.text3,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalTempestiaColors.current

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = colors.text3,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = colors.text2,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    val colors = LocalTempestiaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = "Error",
            tint = Color.Red.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.failed_to_load),
            color = colors.text1,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            color = colors.text3,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}