package com.example.tempestia.ui.settings.viewModel

import com.example.tempestia.MainDispatcherRule
import com.example.tempestia.repository.WeatherRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: WeatherRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        mockRepository = mockk(relaxed = true)

        every { mockRepository.isCelsiusFlow } returns MutableStateFlow(true)
        every { mockRepository.is24HourFlow } returns MutableStateFlow(false)
        every { mockRepository.themeModeFlow } returns MutableStateFlow("System")
        every { mockRepository.languageFlow } returns MutableStateFlow("en")
        every { mockRepository.locationFlow } returns MutableStateFlow(Pair(31.2, 29.9))

        viewModel = SettingsViewModel(mockRepository)
    }

    @Test
    fun testSettingsViewModel_setCelsius_savesPreference() = runTest {
        // Given: A boolean value for the Celsius preference
        val isCelsius = false

        // When: We call setCelsius on the ViewModel
        viewModel.setCelsius(isCelsius)

        // Then: Verify the ViewModel successfully delegated the save action to the Repository
        coVerify(exactly = 1) { mockRepository.saveIsCelsius(isCelsius) }
    }

    @Test
    fun testSettingsViewModel_showToast_updatesStateCorrectly() = runTest {
        // Given: The ViewModel is initialized and ready

        // When: We trigger a toast/snackbar message with a specific string resource ID
        viewModel.showToast(12345)

        // Then: Assert the state flow is correctly updated with the emitted resource ID
        assertEquals(12345, viewModel.toastMessage.value)
    }

    @Test
    fun testSettingsViewModel_clearToast_setsStateToNull() = runTest {
        // Given: The ViewModel already has an active toast/snackbar message state
        viewModel.showToast(12345)

        // When: We instruct the ViewModel to clear the message state
        viewModel.clearToast()

        // Then: Assert the state flow is completely reset to null
        assertEquals(null, viewModel.toastMessage.value)
    }
}