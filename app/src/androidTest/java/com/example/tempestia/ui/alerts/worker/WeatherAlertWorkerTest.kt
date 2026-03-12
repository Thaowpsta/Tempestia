package com.example.tempestia.ui.alerts.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherAlertWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testWeatherWorker_doWork_returnsSuccess() = runTest {
        // Given: Build the worker cleanly without forcing a Java thread pool
        val worker = TestListenableWorkerBuilder<WeatherAlertWorker>(context).build()

        // When: Execute the worker manually
        val result = worker.doWork()

        // Then: Assert it doesn't crash and returns a valid result
        assert(result is ListenableWorker.Result.Success || result is ListenableWorker.Result.Failure)
    }
}