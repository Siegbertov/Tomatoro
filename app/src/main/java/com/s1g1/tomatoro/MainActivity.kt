package com.s1g1.tomatoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.s1g1.tomatoro.ui.MainAppScreen
import com.s1g1.tomatoro.ui.settings.SettingsViewModel
import com.s1g1.tomatoro.ui.theme.TomatoroTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            settingsViewModel.checkPermissions()

            val userSettings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val isDarkTheme = userSettings?.isDarkMode ?: true

            TomatoroTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    settingsViewModel = settingsViewModel,
                    userSettings = userSettings,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.checkPermissions()
    }
}