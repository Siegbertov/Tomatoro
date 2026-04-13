package com.s1g1.tomatoro.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
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
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
    val route: Any
){

    object Timer : NavigationItem(
        title = "Timer",
        iconOutlined = Icons.Outlined.Timer,
        iconFilled = Icons.Filled.Timer,
        route = Screen.Timer
    )

    object Stats : NavigationItem(
        title = "Stats",
        iconOutlined = Icons.Outlined.PieChart,
        iconFilled = Icons.Filled.PieChart,
        route = Screen.Stats
    )

    object Settings : NavigationItem(
        title = "Settings",
        iconOutlined = Icons.Outlined.Settings,
        iconFilled = Icons.Filled.Settings,
        route = Screen.Settings
    )

    companion object{
        val items = listOf(Timer, Stats, Settings)
    }

}
