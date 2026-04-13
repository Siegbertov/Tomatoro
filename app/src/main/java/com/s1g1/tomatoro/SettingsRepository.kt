package com.s1g1.tomatoro

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>){
    private object PreferenceKeys{
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SESSION_TIME = intPreferencesKey("session_time")
        val SHORT_BREAK_TIME = intPreferencesKey("short_break_time")
        val LONG_BREAK_TIME = intPreferencesKey("long_break_time")
        val MAIN_THEME_COLOR = stringPreferencesKey("main_theme_color")
    }
    val settingsFlow: Flow<UserSettings> = dataStore.data
        .map { preferences ->
            UserSettings(
                isDarkMode = preferences[PreferenceKeys.IS_DARK_MODE] ?: false,
                sessionTime = preferences[PreferenceKeys.SESSION_TIME] ?: TimerMode.TOMATORO.defaultDuration,
                shortBreakTime = preferences[PreferenceKeys.SHORT_BREAK_TIME] ?: TimerMode.BREAK.defaultDuration,
                longBreakTime = preferences[PreferenceKeys.LONG_BREAK_TIME] ?: TimerMode.LONG_BREAK.defaultDuration,
                mainThemeColor = preferences[PreferenceKeys.MAIN_THEME_COLOR] ?: MainThemeColors.getDefault().name
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

    suspend fun setMainThemeColor(newMainColor: String){
        dataStore.edit { it[PreferenceKeys.MAIN_THEME_COLOR] = newMainColor }
    }

}

data class UserSettings(
    val isDarkMode: Boolean,
    val sessionTime: Int,
    val shortBreakTime: Int,
    val longBreakTime: Int,
    val mainThemeColor: String,
)

enum class MainThemeColors(
    @StringRes val title: Int,
    val color: Color,
){
    RED( title = R.string.color_red, color = Color(0xFFFF0000) ),
    ORANGE(title = R.string.color_orange, color = Color(0xFFFFB74D)),
    YELLOW(title = R.string.color_yellow, color = Color(0xFFFFF176)),
    GREEN( title = R.string.color_green, color = Color(0xFF00FF00) ),
    BLUE( title = R.string.color_blue, color = Color(0xFF0000FF) ),
    PURPLE(title = R.string.color_purple, color = Color(0xFFBA68C8)),
    PINK(title = R.string.color_pink, color = Color(0xFFF06292)),
    BLACK( title = R.string.color_black, color = Color(0xFF000000) );

    companion object{

        fun fromName(name: String): MainThemeColors {
            return entries.find{ it.name == name } ?: getDefault()
        }
        fun getDefault(): MainThemeColors = MainThemeColors.RED
    }
}