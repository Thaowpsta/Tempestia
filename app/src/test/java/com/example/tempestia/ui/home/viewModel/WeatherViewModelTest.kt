package com.example.tempestia.ui.home.viewModel

import com.example.tempestia.MainDispatcherRule
import com.example.tempestia.data.weather.model.CurrentWeather
import com.example.tempestia.data.weather.model.WeatherResponse
import com.example.tempestia.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: WeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)

        every { mockRepository.isCelsiusFlow } returns MutableStateFlow(true)
        every { mockRepository.is24HourFlow } returns MutableStateFlow(false)
        every { mockRepository.languageFlow } returns MutableStateFlow("en")

        viewModel = WeatherViewModel(mockRepository)
    }

    @Test
    fun testWeatherViewModel_getWeatherOfflineNoCache_returnsErrorState() = runTest {
        // Given: Device is offline and there is no cached city data
        coEvery { mockRepository.isNetworkAvailable() } returns false
        coEvery { mockRepository.getCityByLatLng(any(), any()) } returns null

        // When: We request the weather for specific coordinates
        viewModel.getWeather(31.2, 29.9)

        // Then: Assert the state changes to Error with the correct message
        val currentState = viewModel.weatherState.value
        assertTrue(currentState is WeatherState.Error)
        assertEquals("No internet connection. Please connect to view weather.", (currentState as WeatherState.Error).message)
    }

    @Test
    fun testWeatherViewModel_getWeatherNetworkSucceeds_returnsSuccessState() = runTest {
        // Given: The network is online and returns valid weather data
        val fakeCurrent = CurrentWeather(
            dt = 1000L, sunrise = 0L, sunset = 0L, temp = 25.0, feelsLike = 26.0,
            pressure = 1012, humidity = 50, visibility = 10000, windSpeed = 5.0,
            uvi = 5.0, weather = emptyList()
        )

        val fakeWeather = WeatherResponse(
            current = fakeCurrent, hourly = emptyList(), daily = emptyList(),
            timezone = "Africa/Cairo", alerts = emptyList()
        )

        coEvery { mockRepository.isNetworkAvailable() } returns true
        coEvery { mockRepository.getWeather(any(), any(), any()) } returns Response.success(fakeWeather)
        coEvery { mockRepository.getPreciseLocationName(any(), any(), any()) } returns "Alexandria"
        coEvery { mockRepository.getCityByLatLng(any(), any()) } returns null // Bypass cache updating for this test

        // When: We request the weather for specific coordinates
        viewModel.getWeather(31.2, 29.9)

        // Then: Assert the state is Success and contains the expected city name
        val currentState = viewModel.weatherState.value
        assertTrue(currentState is WeatherState.Success)
        assertEquals("Alexandria", (currentState as WeatherState.Success).cityName)
    }
}