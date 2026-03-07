package com.example.tempestia.data.alerts.dataSource.locale

import android.content.Context
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow

class AlertsLocalDatasource(context: Context) {
    private val alertsDao = AppDatabase.getDatabase(context).alertsDao()

    fun getAllAlerts(): Flow<List<Alert>> = alertsDao.getAllAlerts()
    suspend fun insertAlert(alert: Alert) = alertsDao.insertAlert(alert)
    suspend fun deleteAlert(id: String) = alertsDao.deleteAlert(id)
}