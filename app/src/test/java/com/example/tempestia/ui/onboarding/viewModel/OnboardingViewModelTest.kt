package com.example.tempestia.ui.onboarding.viewModel

import com.example.tempestia.MainDispatcherRule
import com.example.tempestia.data.WeatherRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: WeatherRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)

        every { mockRepository.isOnboardingCompleted } returns MutableStateFlow(false)
        every { mockRepository.locationFlow } returns MutableStateFlow(null)

        viewModel = OnboardingViewModel(mockRepository)
    }

    @Test
    fun testOnboardingViewModel_completeOnboarding_callsRepositoryToSaveStatus() = runTest {
        // Given: The ViewModel is initialized with the mocked repository (via setup)

        // When: We trigger the completion of the onboarding process
        viewModel.completeOnboarding()

        // Then: Assert that the repository's completeOnboarding function is called exactly once
        coVerify(exactly = 1) { mockRepository.completeOnboarding() }
    }

    @Test
    fun testOnboardingViewModel_saveLocation_savesCoordinatesAndSetsCurrent() = runTest {
        // Given: The ViewModel is ready to save a newly selected location

        // When: We call saveLocation with specific coordinates and a city name
        viewModel.saveLocation(31.2, 29.9, "Alexandria")

        // Then: Assert the repository saves the raw coordinates
        coVerify(exactly = 1) { mockRepository.saveLocation(31.2, 29.9) }

        // And Then: Assert the repository creates a FavoriteCity object and sets it as the current location
        coVerify(exactly = 1) {
            mockRepository.setCityAsCurrent(match {
                it.cityName == "Alexandria" && it.lat == 31.2 && it.lon == 29.9 && it.isCurrentLocation
            })
        }
    }
}