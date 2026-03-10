package com.example.tempestia.ui.alerts.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.repository.WeatherRepository
import com.example.tempestia.ui.alerts.worker.AlarmScheduler
import com.example.tempestia.ui.alerts.worker.NotificationType
import com.example.tempestia.ui.alerts.worker.WeatherAlertWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

class AlertsViewModel(
    private val repository: WeatherRepository,
    context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    private val _alertToEdit = MutableStateFlow<SubscribedAlert?>(null)
    val alertToEdit: StateFlow<SubscribedAlert?> = _alertToEdit.asStateFlow()

    private val _templateToAdd = MutableStateFlow<AlertItem?>(null)
    val templateToAdd: StateFlow<AlertItem?> = _templateToAdd.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Always true on Android 12 and below
        }
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    fun setShowAddSheet(show: Boolean) { _showAddSheet.value = show }
    fun setAlertToEdit(alert: SubscribedAlert?) { _alertToEdit.value = alert }
    fun setTemplateToAdd(template: AlertItem?) { _templateToAdd.value = template }
    fun setHasNotificationPermission(granted: Boolean) { _hasNotificationPermission.value = granted }

    val subscribedAlerts: StateFlow<List<SubscribedAlert>> = repository.getSubscribedAlerts()
        .map { entities ->
            entities.map { entity ->
                SubscribedAlert(
                    id = entity.id,
                    title = entity.title,
                    subtitle = entity.subtitle,
                    level = AlertLevel.valueOf(entity.level),
                    notificationType = NotificationType.valueOf(entity.notificationType),
                    isActive = entity.isActive,
                    timeHour = entity.timeHour,
                    timeMinute = entity.timeMinute
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

    fun addAlert(template: AlertItem, notifType: NotificationType, hour: Int? = null, minute: Int? = null) {
        viewModelScope.launch {
            val newEntity = Alert(
                id = UUID.randomUUID().toString(),
                title = template.title,
                subtitle = template.subtitle,
                level = template.level.name,
                notificationType = notifType.name,
                isActive = true,
                timeHour = hour,
                timeMinute = minute
            )
            repository.insertAlert(newEntity)
            scheduleWeatherWorker()
        }
    }

    fun updateAlert(
        alert: SubscribedAlert,
        newType: NotificationType? = null,
        isActive: Boolean? = null,
        hour: Int? = null,
        minute: Int? = null,
        context: Context
    ) {
        viewModelScope.launch {
            val finalType = newType ?: alert.notificationType
            val finalHour = hour ?: alert.timeHour
            val finalMinute = minute ?: alert.timeMinute
            val finalActive = isActive ?: alert.isActive

            val updatedEntity = Alert(
                id = alert.id,
                title = alert.title,
                subtitle = alert.subtitle,
                level = alert.level.name,
                notificationType = finalType.name,
                isActive = finalActive,
                timeHour = finalHour,
                timeMinute = finalMinute
            )
            repository.insertAlert(updatedEntity)

            if (finalActive && finalType == NotificationType.ALARM && finalHour != null && finalMinute != null) {
                AlarmScheduler.scheduleAlarm(context, alert.id, alert.title, finalHour, finalMinute)
            } else {
                AlarmScheduler.cancelAlarm(context, alert.id)
            }
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