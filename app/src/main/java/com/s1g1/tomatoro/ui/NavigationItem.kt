package com.s1g1.tomatoro.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


sealed class Screen {

    @Serializable
    object Timer : Screen()

    @Serializable
    object Stats : Screen()

    @Serializable
    object Settings : Screen()
}

sealed class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
){

    object Timer : NavigationItem(
        title = "Timer",
        icon = Icons.Default.Timer,
        route = Screen.Timer
    )

    object Stats : NavigationItem(
        title = "Stats",
        icon = Icons.Default.BarChart,
        route = Screen.Stats
    )

    object Settings : NavigationItem(
        title = "Settings",
        icon = Icons.Default.Settings,
        route = Screen.Settings
    )

    companion object{
        val items = listOf(Timer, Stats, Settings)
    }

}
