package com.example.tempestia.data.alerts.dataSource.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tempestia.data.weather.dataSource.locale.AlertsDao
import com.example.tempestia.data.weather.model.Alert
import com.example.tempestia.data.db.AppDatabase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertsDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AlertsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.alertsDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAlertsDao_insertAndReadAlert_returnsCorrectData() = runTest {
        // Given: Create a test alert object to insert
        val testAlert = Alert(
            id = "alert_morning",
            title = "Morning Summary",
            subtitle = "Daily brief",
            level = "Info",
            notificationType = "PUSH",
            isActive = true
        )

        // When: Insert the alert into the database and retrieve the flow's first emission
        dao.insertAlert(testAlert)
        val alertsList = dao.getAllAlerts().first()

        // Then: Assert the database contains exactly 1 item with the correct properties
        assertEquals(1, alertsList.size)
        assertEquals("Morning Summary", alertsList[0].title)
        assertEquals(true, alertsList[0].isActive)
    }

    @Test
    fun testAlertsDao_updateAlert_changesActiveStatus() = runTest {
        // Given: Insert an initial active alert into the database
        val testAlert = Alert(
            id = "alert_1",
            title = "Rain",
            subtitle = "",
            level = "Warning",
            notificationType = "SILENT",
            isActive = true
        )
        dao.insertAlert(testAlert)

        // When: Update the alert's active status to false and re-insert it
        val updatedAlert = testAlert.copy(isActive = false)
        dao.insertAlert(updatedAlert)

        // Then: Assert the updated status is reflected in the database
        val alertsList = dao.getAllAlerts().first()
        assertEquals(false, alertsList[0].isActive)
    }
}