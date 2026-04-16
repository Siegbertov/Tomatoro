package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.s1g1.tomatoro.MainThemeColors
import com.s1g1.tomatoro.ui.settings.SettingsViewModel
import com.s1g1.tomatoro.UserSettings
import com.s1g1.tomatoro.ui.settings.SettingsScreen
import com.s1g1.tomatoro.ui.stats.StatsScreen
import com.s1g1.tomatoro.ui.stats.StatsViewModel
import com.s1g1.tomatoro.ui.timer.TimerScreen
import com.s1g1.tomatoro.ui.timer.TimerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainAppScreen(
    settingsViewModel: SettingsViewModel,
    userSettings: UserSettings?,
) {
    val navController = rememberNavController()
    val badgeCount by settingsViewModel.badgeCount.collectAsStateWithLifecycle()
    val timerViewModel: TimerViewModel = koinViewModel()
    val statsViewModel: StatsViewModel = koinViewModel()
    val currentColor: Color = MainThemeColors.fromName(
        name = userSettings?.mainThemeColor ?: MainThemeColors.getDefault().name
    ).color


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { SimpleTopBar() },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            NavigationBottomFAB(
                navController=navController,
                currentColor=currentColor,
                settingsBadgeCount = badgeCount
            )
        }
    ) { innerPadding ->
        NavHost(
            navController= navController,
            startDestination = Screen.Timer,
            modifier = Modifier.padding(innerPadding)
            ){
            composable<Screen.Timer>{
                TimerScreen(
                    userSettings = userSettings,
                    timerViewModel = timerViewModel
                )
            }
            composable<Screen.Stats>{
                StatsScreen(
                    statsViewModel = statsViewModel
                )
            }
            composable<Screen.Settings>{
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    userSettings = userSettings
                )
            }
        }
    }
}








