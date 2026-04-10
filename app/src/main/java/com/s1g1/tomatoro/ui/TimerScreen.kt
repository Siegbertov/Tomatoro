package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.s1g1.tomatoro.UserSettings
import com.s1g1.tomatoro.TimerMode

@Composable
fun TimerScreen(
    navController: NavHostController,
    userSettings: UserSettings?
) {
    val currentRoute = navController.currentBackStackEntryAsState().value
        ?.destination?.route
        ?.substringAfterLast(".") ?: "Loading..."

    var selectedMode by remember { mutableStateOf(TimerMode.TOMATORO) }

    val currentTime = when(selectedMode){
        TimerMode.TOMATORO -> userSettings?.sessionTime?:TimerMode.TOMATORO.defaultDuration
        TimerMode.BREAK -> userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
        TimerMode.LONG_BREAK -> userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        Text(text = currentRoute)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            TimerMode.entries.forEach { mode->
                FilterChip(
                    selected = selectedMode==mode,
                    onClick = {selectedMode = mode},
                    label = { Text( text = stringResource(mode.title))}
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Text(text=currentTime.toString())
        }
    }
}