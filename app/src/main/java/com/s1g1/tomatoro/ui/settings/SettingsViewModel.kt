package com.s1g1.tomatoro.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.provider.Settings
import androidx.core.net.toUri

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val application: Application
) : ViewModel(){

    val settings = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isNotificationAllowed = MutableStateFlow(false)
    val isNotificationAllowed = _isNotificationAllowed.asStateFlow()

    private val _isRunInBackgroundAllowed = MutableStateFlow(false)
    val isRunInBackgroundAllowed = _isRunInBackgroundAllowed.asStateFlow()

    val badgeCount: StateFlow<Int> = combine(_isNotificationAllowed, _isRunInBackgroundAllowed) { notify, battery ->
        var count = 0
        if (!notify) count++
        if (!battery) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun checkPermissions() {
        val context = application.applicationContext
        // CHECK NOTIFICATIONS ALLOWED
        _isNotificationAllowed.value = NotificationManagerCompat.from(context).areNotificationsEnabled()

        // CHECK RUN IN BACKGROUND ALLOWED
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        _isRunInBackgroundAllowed.value = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun onNotificationClick() {
        val context = application.applicationContext
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @SuppressLint("BatteryLife")
    fun onRunInBackgroundClick() {
        val context = application.applicationContext
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


    fun updateTheme(newTheme: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setDarkMode(enabled = newTheme)
        }
    }

    fun updateSessionTime(newTime: Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSessionTime(newTime=newTime)
        }
    }

    fun updateShortBreakTime(newTime: Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setShortBreakTime(newTime=newTime)
        }
    }

    fun updateLongBreakTime(newTime: Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setLongBreakTime(newTime=newTime)
        }
    }

    fun updateMainThemeColor(newMainColor: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setMainThemeColor(newMainColor = newMainColor)
        }
    }

}