package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.s1g1.tomatoro.UserSettings

@Composable
fun TimerScreen(navController: NavHostController, userSettings: UserSettings?) {
    val currentRoute = navController.currentBackStackEntryAsState().value
        ?.destination?.route
        ?.substringAfterLast(".") ?: "Loading..."

    val sessionTime = userSettings?.sessionTime?:25
    val shortBreakTime = userSettings?.shortBreakTime?:5
    val longBreakTime = userSettings?.longBreakTime?:15

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text = currentRoute)
        Text(text= sessionTime.toString())
        Text(text= shortBreakTime.toString())
        Text(text= longBreakTime.toString())
    }
}