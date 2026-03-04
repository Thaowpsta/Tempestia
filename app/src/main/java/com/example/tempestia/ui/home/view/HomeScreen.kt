package com.example.tempestia.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.data.forecast.model.WeatherResponse
import com.example.tempestia.ui.home.viewModel.WeatherState
import com.example.tempestia.ui.home.viewModel.WeatherViewModel
import com.example.tempestia.ui.onboarding.view.AnimatedParticleBackground
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import java.util.Locale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel = viewModel(),
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    val colors = LocalTempestiaColors.current
    val weatherState by weatherViewModel.weatherState.collectAsState()

    val userLocation by onboardingViewModel.locationFlow.collectAsState(initial = null)

    LaunchedEffect(userLocation) {
        userLocation?.let { (lat, lng) ->
            weatherViewModel.getWeather(lat, lng)
        } ?: run {
            weatherViewModel.getWeather(lat = 31.2001, lon = 29.9187)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.bgDeep)) {

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
                    WeatherDashboard(state.weatherData)
                }

                is WeatherState.Error -> {
                    ErrorScreen(state.message)
                }
            }
        }
    }
}

@Composable
fun WeatherDashboard(data: WeatherResponse) {
    val colors = LocalTempestiaColors.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.background(colors.glass, RoundedCornerShape(50.dp))
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
                text = data.cityName,
                color = colors.text1,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("⛅", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = buildAnnotatedString {
                append("${data.main.temp.toInt()}")
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

        val condition = data.weather.firstOrNull()?.description ?: "Unknown"
        Text(
            text = condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text2
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp))
                .background(colors.glass, RoundedCornerShape(32.dp))
                .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp))
                .padding(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val windSpeedKmh = (data.wind.speed * 3.6).toInt()

                MetricItem("FEELS LIKE", "${data.main.feelsLike.toInt()}°", Modifier.weight(1f))
                MetricItem("HUMIDITY", "${data.main.humidity}%", Modifier.weight(1f))
                MetricItem("WIND", "$windSpeedKmh km/h", Modifier.weight(1f))
            }
        }
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
fun WeatherDetailCard(title: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalTempestiaColors.current

    Column(
        modifier = modifier.clip(RoundedCornerShape(32.dp)).background(colors.bgCard)
            .border(1.dp, colors.glassBorder, RoundedCornerShape(32.dp)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = colors.text3,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 28.sp,
            color = colors.text1,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    val colors = LocalTempestiaColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
            "Failed to load weather",
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