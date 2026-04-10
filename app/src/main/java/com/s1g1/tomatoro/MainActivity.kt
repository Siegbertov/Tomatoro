package com.s1g1.tomatoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.s1g1.tomatoro.ui.MainAppScreen
import com.s1g1.tomatoro.ui.theme.TomatoroTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val settingsViewModel: SettingsViewModel = koinViewModel()
            val userSettings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val isDarkTheme = userSettings?.isDarkMode ?: true

            TomatoroTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    settingsViewModel = settingsViewModel,
                    userSettings = userSettings,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { settingsViewModel.updateTheme(newTheme = !isDarkTheme) }
                )
            }
        }
    }
}