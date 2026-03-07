package com.example.tempestia.ui.alerts.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.repository.WeatherRepository
import com.example.tempestia.worker.NotificationType
import com.example.tempestia.worker.WeatherAlertWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class AlertsViewModel(
    private val repository: WeatherRepository,
    context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    val subscribedAlerts: StateFlow<List<SubscribedAlert>> = repository.getSubscribedAlerts()
        .map { entities ->
            entities.map { entity ->
                SubscribedAlert(
                    id = entity.id,
                    title = entity.title,
                    subtitle = entity.subtitle,
                    level = AlertLevel.valueOf(entity.level),
                    notificationType = NotificationType.valueOf(entity.notificationType),
                    isActive = entity.isActive
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val baseTemplates = listOf(
        AlertItem("Severe Thunderstorm", "Heavy rain and strong winds", AlertLevel.DANGER),
        AlertItem("Extreme Heat", "Temperatures exceeding 40°C", AlertLevel.WARNING),
        AlertItem("Rain Reminder", "Notifies you if rain is expected today", AlertLevel.INFO),
        AlertItem("Morning Summary", "Daily forecast at 7:00 AM", AlertLevel.INFO),
        AlertItem("Custom Condition", "Set your own weather rules", AlertLevel.INFO)
    )

    val availableTemplatesFlow: StateFlow<List<AlertItem>> = subscribedAlerts.map { subscribed ->
        baseTemplates.filter { template ->
            subscribed.none { it.title == template.title }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), baseTemplates)

    fun addAlert(template: AlertItem, notifType: NotificationType) {
        viewModelScope.launch {
            val newEntity = Alert(
                id = UUID.randomUUID().toString(),
                title = template.title,
                subtitle = template.subtitle,
                level = template.level.name,
                notificationType = notifType.name,
                isActive = true
            )
            repository.insertAlert(newEntity)
            scheduleWeatherWorker()
        }
    }

    fun updateAlert(alert: SubscribedAlert, newType: NotificationType? = null, isActive: Boolean? = null) {
        viewModelScope.launch {
            val updatedEntity = Alert(
                id = alert.id,
                title = alert.title,
                subtitle = alert.subtitle,
                level = alert.level.name,
                notificationType = (newType ?: alert.notificationType).name,
                isActive = isActive ?: alert.isActive
            )
            repository.insertAlert(updatedEntity)
        }
    }

    fun removeAlert(id: String) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    private fun scheduleWeatherWorker() {
        val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(2, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            "WeatherAlertJob",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}

class AlertsViewModelFactory(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}