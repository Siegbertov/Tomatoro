package com.s1g1.tomatoro

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>){
    private object PreferenceKeys{
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SESSION_TIME = intPreferencesKey("session_time")
        val SHORT_BREAK_TIME = intPreferencesKey("short_break_time")
        val LONG_BREAK_TIME = intPreferencesKey("long_break_time")
    }
    val settingsFlow: Flow<UserSettings> = dataStore.data
        .map { preferences ->
            UserSettings(
                isDarkMode = preferences[PreferenceKeys.IS_DARK_MODE] ?: false,
                sessionTime = preferences[PreferenceKeys.SESSION_TIME] ?: TimerMode.TOMATORO.defaultDuration,
                shortBreakTime = preferences[PreferenceKeys.SHORT_BREAK_TIME] ?: TimerMode.BREAK.defaultDuration,
                longBreakTime = preferences[PreferenceKeys.LONG_BREAK_TIME] ?: TimerMode.LONG_BREAK.defaultDuration
            )
        }

    suspend fun setDarkMode(enabled: Boolean){
        dataStore.edit { it[PreferenceKeys.IS_DARK_MODE] = enabled}
    }

    suspend fun setSessionTime(newTime: Int){
        dataStore.edit { it[PreferenceKeys.SESSION_TIME] = newTime }
    }

    suspend fun setShortBreakTime(newTime: Int){
        dataStore.edit { it[PreferenceKeys.SHORT_BREAK_TIME] = newTime }
    }

    suspend fun setLongBreakTime(newTime: Int){
        dataStore.edit { it[PreferenceKeys.LONG_BREAK_TIME] = newTime }
    }

}

data class UserSettings(
    val isDarkMode: Boolean,
    val sessionTime: Int,
    val shortBreakTime: Int,
    val longBreakTime: Int
)