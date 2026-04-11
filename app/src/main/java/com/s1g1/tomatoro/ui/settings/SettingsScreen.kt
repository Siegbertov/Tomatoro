package com.s1g1.tomatoro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.s1g1.tomatoro.R
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.UserSettings

@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    userSettings: UserSettings?
) {    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(bottom=80.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){

        ThemeSettingsComponent(
            userSettings=userSettings,
            settingsViewModel=settingsViewModel
        )

        DurationSettingsComponent(
            userSettings=userSettings,
            settingsViewModel=settingsViewModel
        )

    }
}

@Composable
fun ThemeSettingsComponent(
    userSettings: UserSettings?,
    settingsViewModel: SettingsViewModel
) {
    val isDarkTheme = userSettings?.isDarkMode ?: true

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical=10.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(
                text=stringResource(R.string.dark_theme)
            )

            Switch(
                checked = isDarkTheme,
                onCheckedChange = { settingsViewModel.updateTheme(newTheme = !isDarkTheme) },
                thumbContent = {
                    if (isDarkTheme) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun DurationSettingsComponent(
    userSettings: UserSettings?,
    settingsViewModel: SettingsViewModel
) {
    val sessionTime = userSettings?.sessionTime?: TimerMode.TOMATORO.defaultDuration
    val shortBreakTime = userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
    val longBreakTime = userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical=10.dp, horizontal = 10.dp),
    ){
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(R.string.settings_duration),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth()
            ){

                DurationCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    duration = sessionTime.toString(),
                    durationDescription = stringResource(R.string.tomatoro_session),
                    onToggleIncrease = {
                        if (sessionTime<60){
                            settingsViewModel.updateSessionTime(newTime = sessionTime+1)
                        }
                    },
                    onToggleDecrease = {
                        if (sessionTime>1){
                            settingsViewModel.updateSessionTime(newTime = sessionTime-1)
                        }
                    }
                )

                DurationCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    duration = shortBreakTime.toString(),
                    durationDescription = stringResource(R.string.short_break_session),
                    onToggleIncrease = {
                        if (shortBreakTime<60){
                            settingsViewModel.updateShortBreakTime(newTime = shortBreakTime+1)
                        }
                    },
                    onToggleDecrease = {
                        if (shortBreakTime>1){
                            settingsViewModel.updateShortBreakTime(newTime = shortBreakTime-1)
                        }
                    }
                )

                DurationCard(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .heightIn(max = 100.dp)
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    duration = longBreakTime.toString(),
                    durationDescription = stringResource(R.string.long_break_session),
                    onToggleIncrease = {
                        if (longBreakTime<60){
                            settingsViewModel.updateLongBreakTime(newTime = longBreakTime+1)
                        }
                    },
                    onToggleDecrease = {
                        if (longBreakTime>1){
                            settingsViewModel.updateLongBreakTime(newTime = longBreakTime-1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DurationCard(
    modifier: Modifier = Modifier,
    duration: String,
    durationDescription: String,
    onToggleIncrease: ()-> Unit,
    onToggleDecrease: ()-> Unit,
){
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = null,
                    modifier = Modifier.clickable( onClick = { onToggleIncrease() } )
                )
                Text(text = duration, fontSize = 20.sp)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable( onClick = { onToggleDecrease() } )
                )
                Text(text = durationDescription)
            }
        }
    }
}
