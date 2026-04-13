package com.s1g1.tomatoro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel(){

    val settings = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = null
    )

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