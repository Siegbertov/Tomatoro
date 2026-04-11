package com.s1g1.tomatoro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.s1g1.tomatoro.UserSettings
import com.s1g1.tomatoro.TimerMode
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Composable
fun TimerScreen(
    navController: NavHostController,
    userSettings: UserSettings?,
    timerViewModel: TimerViewModel
) {
    val currentRoute = navController.currentBackStackEntryAsState().value
        ?.destination?.route
        ?.substringAfterLast(".") ?: "Loading..."

    var selectedMode by remember { mutableStateOf(TimerMode.TOMATORO) }
    val selectedModeString = stringResource(selectedMode.title)

    val currentTime = when(selectedMode){
        TimerMode.TOMATORO -> userSettings?.sessionTime?:TimerMode.TOMATORO.defaultDuration
        TimerMode.BREAK -> userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
        TimerMode.LONG_BREAK -> userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration
    }

    val timeLeft by timerViewModel.timeLeft.collectAsStateWithLifecycle()
    val isRunning by timerViewModel.isRunning.collectAsStateWithLifecycle()

    LaunchedEffect(userSettings, selectedMode){
        if (!isRunning){
            val initialTime = when(selectedMode){
                TimerMode.TOMATORO -> userSettings?.sessionTime?:TimerMode.TOMATORO.defaultDuration
                TimerMode.BREAK -> userSettings?.shortBreakTime?:TimerMode.BREAK.defaultDuration
                TimerMode.LONG_BREAK -> userSettings?.longBreakTime?:TimerMode.LONG_BREAK.defaultDuration
            }
            timerViewModel.resetTimer(seconds =initialTime * 60L, manual = false)
        }
    }

    Column(
        modifier = Modifier
            .padding(bottom=80.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        ModeSelectorComponent(
            isRunning=isRunning,
            selectedMode=selectedMode,
            onModeChange={ newMode-> selectedMode = newMode}
        )

        ActualTimerComponent(
            timeLeft=timeLeft,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        TimerButtonsComponent(
            isRunning=isRunning,
            onStartPause = { timerViewModel.onStartPausePressed(timeLeft = timeLeft, modeString=selectedModeString) },
            onReset = { timerViewModel.onResetPressed(resetTime = currentTime*60L) }
        )


    }
}

@Composable
fun TimerButtonsComponent(
    isRunning: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ){
        OutlinedButton(
            onClick = { onStartPause() },
            modifier = Modifier.padding(horizontal=10.dp)
        ) {
            Icon(
                imageVector = if(isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Text(if(isRunning) "PAUSE" else "START")
        }

        Button(
            onClick= { onReset() },
            modifier = Modifier.padding(horizontal=10.dp)
            ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Text("RESET")
        }
    }
}

@Composable
fun ActualTimerComponent(
    timeLeft: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal=10.dp),
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = TimerViewModel.formatTime(timeLeft),
                    style = TextStyle(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color=Color.Black
                )
            }
        }
    }
}

@Composable
fun ModeSelectorComponent(
    isRunning: Boolean,
    selectedMode: TimerMode,
    onModeChange: (TimerMode) -> Unit
) {
    AnimatedVisibility(
        visible = !isRunning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            TimerMode.entries.forEach { mode->
                FilterChip(
                    selected = selectedMode==mode,
                    onClick = {
                        if (!isRunning){
                            onModeChange(mode)
                        }
                    },
                    label = { Text( text = stringResource(mode.title))}
                )
            }
        }
    }
}