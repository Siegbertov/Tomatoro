package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainAppScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SimpleTopBar(
                isDarkTheme = isDarkTheme,
                onToggleTheme = {onToggleTheme()}
            )

        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            NavigationBottomToolbar(
                navController=navController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController= navController,
            startDestination = Screen.Timer,
            modifier = Modifier.padding(innerPadding)
            ){
            composable<Screen.Timer>{ TimerScreen(navController=navController) }
            composable<Screen.Stats>{ StatsScreen(navController=navController) }
            composable<Screen.Settings>{ SettingsScreen(navController=navController) }
        }
    }
}








