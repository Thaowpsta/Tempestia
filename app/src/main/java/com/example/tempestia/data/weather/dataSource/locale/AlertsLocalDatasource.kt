package com.example.tempestia.data.weather.dataSource.locale

import com.example.tempestia.data.weather.model.Alert
import kotlinx.coroutines.flow.Flow

class AlertsLocalDatasource(private val alertsDao: AlertsDao) {

    fun getAllAlerts(): Flow<List<Alert>> = alertsDao.getAllAlerts()
    suspend fun insertAlert(alert: Alert) = alertsDao.insertAlert(alert)
    suspend fun deleteAlert(id: String) = alertsDao.deleteAlert(id)
}