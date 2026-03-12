package com.example.tempestia.data

import android.content.Context
import com.example.tempestia.MainDispatcherRule
import com.example.tempestia.data.alerts.dataSource.locale.AlertsLocalDatasource
import com.example.tempestia.data.favorites.dataSource.local.FavoritesLocalDatasource
import com.example.tempestia.data.favorites.model.FavoriteCity
import com.example.tempestia.data.settings.dataSource.locale.SettingsLocalDatasource
import com.example.tempestia.data.weather.dataSource.remote.WeatherRemoteDatasource
import com.example.tempestia.data.weather.model.CurrentWeather
import com.example.tempestia.data.weather.model.WeatherResponse
import com.example.tempestia.repository.WeatherRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class WeatherRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: WeatherRepository
    private lateinit var mockContext: Context

    private lateinit var mockRemoteDatasource: WeatherRemoteDatasource
    private lateinit var mockFavoritesDatasource: FavoritesLocalDatasource
    private lateinit var mockAlertsDatasource: AlertsLocalDatasource
    private lateinit var mockSettingsDatasource: SettingsLocalDatasource

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)

        mockRemoteDatasource = mockk(relaxed = true)
        mockFavoritesDatasource = mockk(relaxed = true)
        mockAlertsDatasource = mockk(relaxed = true)
        mockSettingsDatasource = mockk(relaxed = true)

        every { mockSettingsDatasource.isOnboardingCompleted } returns MutableStateFlow(true)
        every { mockSettingsDatasource.locationFlow } returns MutableStateFlow(null)
        every { mockSettingsDatasource.isCelsiusFlow } returns MutableStateFlow(true)
        every { mockSettingsDatasource.is24HourFlow } returns MutableStateFlow(false)
        every { mockSettingsDatasource.themeModeFlow } returns MutableStateFlow("System")
        every { mockSettingsDatasource.languageFlow } returns MutableStateFlow("en")

        repository = WeatherRepository(
            context = mockContext,
            remoteDatasource = mockRemoteDatasource,
            favoritesLocalDatasource = mockFavoritesDatasource,
            alertsLocalDatasource = mockAlertsDatasource,
            settingsLocalDatasource = mockSettingsDatasource
        )
    }

    @Test
    fun testWeatherRepository_getWeather_routesToRemoteDatasource() = runTest {
        // Given: Create fake response data and mock the remote datasource
        val fakeCurrent = CurrentWeather(1000L, 0L, 0L, 25.0, 26.0, 1012, 50, 10000, 5.0, 5.0, emptyList())
        val fakeResponse = Response.success(WeatherResponse(fakeCurrent, emptyList(), emptyList(), "Africa/Cairo", emptyList()))

        coEvery { mockRemoteDatasource.getCurrentWeather(any(), any(), any()) } returns fakeResponse

        // When: We request weather from the repository
        val result = repository.getWeather(31.2, 29.9, "fake_api_key")

        // Then: Assert the result matches and the remote datasource was called
        assertEquals(fakeResponse, result)
        coVerify(exactly = 1) { mockRemoteDatasource.getCurrentWeather(31.2, 29.9, "fake_api_key") }
    }

    @Test
    fun testWeatherRepository_insertFavorite_routesToFavoritesLocalDatasource() = runTest {
        // Given: Create a fake city and mock the local datasource behavior
        val fakeCity = FavoriteCity(cityName = "Alexandria", lat = 31.2, lon = 29.9)
        coEvery { mockFavoritesDatasource.insertFavorite(any()) } just Runs

        // When: We insert a favorite city via the repository
        repository.insertFavorite(fakeCity)

        // Then: Assert the repository correctly routed the call to the favorites datasource
        coVerify(exactly = 1) { mockFavoritesDatasource.insertFavorite(fakeCity) }
    }

    @Test
    fun testWeatherRepository_saveLanguage_routesToSettingsLocalDatasource() = runTest {
        // Given: Mock the settings datasource behavior
        coEvery { mockSettingsDatasource.saveLanguage(any()) } just Runs

        // When: We request to save a new language via the repository
        repository.saveLanguage("ar")

        // Then: Assert the repository correctly routed the call to the settings datasource
        coVerify(exactly = 1) { mockSettingsDatasource.saveLanguage("ar") }
    }
}