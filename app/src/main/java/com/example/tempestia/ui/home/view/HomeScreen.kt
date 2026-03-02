package com.example.tempestia.ui.home.view

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempestia.ui.home.viewModel.WeatherState
import com.example.tempestia.ui.home.viewModel.WeatherViewModel
import com.example.tempestia.ui.onboarding.viewModel.OnboardingViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel = viewModel(),
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()

    val location by onboardingViewModel.locationFlow.collectAsState(initial = null)

    LaunchedEffect(location) {
        location?.let { (lat, lng) ->
            Log.d("WeatherAPI", "Fetching weather for saved location: $lat, $lng")
            weatherViewModel.getWeather(lat = lat, lon = lng)
        } ?: run {
            Log.d("WeatherAPI", "Fetching weather for default location...")
            weatherViewModel.getWeather(lat = 31.2001, lon = 29.9187)
        }
    }
    
    LaunchedEffect(weatherState) {
        when (val state = weatherState) {
            is WeatherState.Idle -> Log.d("WeatherAPI", "State: Idle")
            is WeatherState.Loading -> Log.d("WeatherAPI", "State: Loading from network...")
            is WeatherState.Success -> {
                Log.d("WeatherAPI", "✅ SUCCESS!")
                Log.d("WeatherAPI", "City Name: ${state.weatherData.cityName}")
                Log.d("WeatherAPI", "Current Temp: ${state.weatherData.main.temp}°C")
                Log.d("WeatherAPI", "Feels Like: ${state.weatherData.main.feelsLike}°C")
                Log.d("WeatherAPI", "Conditions: ${state.weatherData.weather.firstOrNull()?.description}")
            }
            is WeatherState.Error -> {
                Log.e("WeatherAPI", "❌ ERROR: ${state.message}")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Check Logcat for Weather Data!")
    }
}