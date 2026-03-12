package com.example.tempestia.data.weather.dataSource.remote

import com.example.tempestia.data.network.WeatherService
import com.example.tempestia.data.weather.model.CurrentWeather
import com.example.tempestia.data.weather.model.GeoResponse
import com.example.tempestia.data.weather.model.WeatherResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class WeatherRemoteDatasourceTest {

    private lateinit var mockApiService: WeatherService
    private lateinit var remoteDatasource: WeatherRemoteDatasource

    @Before
    fun setup() {
        mockApiService = mockk()
        remoteDatasource = WeatherRemoteDatasource(weatherService = mockApiService)
    }

    @Test
    fun testWeatherRemoteDatasource_getCurrentWeather_callsApiWithCorrectParameters() = runTest {
        // Given: Create fake responses and mock the API service to return them
        val fakeCurrent = CurrentWeather(1000L, 0L, 0L, 25.0, 26.0, 1012, 50, 10000, 5.0, 5.0, emptyList())
        val fakeResponse = Response.success(WeatherResponse(fakeCurrent, emptyList(), emptyList(), "Africa/Cairo", emptyList()))

        coEvery {
            mockApiService.getCurrentWeather(
                lat = 31.2,
                lon = 29.9,
                apiKey = "fake_key",
                language = any(), // Language changes based on phone, so we accept any string
                exclude = any(),
                units = any()
            )
        } returns fakeResponse

        // When: We call getCurrentWeather on the remote datasource
        val result = remoteDatasource.getCurrentWeather(31.2, 29.9, "fake_key")

        // Then: Assert the result matches the fake response and the API was called with the correct parameters
        assertEquals(fakeResponse, result)
        coVerify(exactly = 1) {
            mockApiService.getCurrentWeather(
                lat = 31.2,
                lon = 29.9,
                apiKey = "fake_key",
                language = any(),
                exclude = any(),
                units = any()
            )
        }
    }

    @Test
    fun testWeatherRemoteDatasource_getCoordinatesByName_callsApiWithCorrectQuery() = runTest {
        // Given: Create a fake geographic coordinate list and mock the API service
        val fakeGeoList = listOf(GeoResponse("Alexandria", 31.2, 29.9, "EG"))
        val fakeResponse = Response.success(fakeGeoList)

        coEvery {
            mockApiService.getCoordinatesByName(
                query = "Alexandria",
                limit = 5,
                apiKey = "fake_key"
            )
        } returns fakeResponse

        // When: We search for coordinates by city name
        val result = remoteDatasource.getCoordinatesByName("Alexandria", "fake_key", 5)

        // Then: Assert the correct response is returned and the API was called with the correct query
        assertEquals(fakeResponse, result)
        coVerify(exactly = 1) {
            mockApiService.getCoordinatesByName(
                query = "Alexandria",
                limit = 5,
                apiKey = "fake_key"
            )
        }
    }
}