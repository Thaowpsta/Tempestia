package com.example.tempestia.ui.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.tempestia.ui.home.viewModel.WeatherState
import com.example.tempestia.ui.home.viewModel.WeatherViewModel
import com.example.tempestia.ui.onboarding.view.LocalTempestiaColors
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel
import com.example.tempestia.utils.AnimatedParticleBackground
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                weatherViewModel.refreshWeather(isSwipeRefresh = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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