package com.s1g1.tomatoro.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.s1g1.tomatoro.UserSettings
import com.s1g1.tomatoro.TimerMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import com.s1g1.tomatoro.MainThemeColors

@Composable
fun TimerScreen(
    navController: NavHostController,
    userSettings: UserSettings?,
    timerViewModel: TimerViewModel
) {

    val context = LocalContext.current
    val currentMainThemeColor: MainThemeColors = MainThemeColors.fromName(
        name = userSettings?.mainThemeColor ?: MainThemeColors.getDefault().name
    )

    var selectedMode by remember { mutableStateOf(TimerMode.TOMATORO) }

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
    Box(
        modifier = Modifier
            .padding(bottom=80.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        ModeSelectorComponent(
            isRunning=isRunning,
            selectedMode=selectedMode,
            currentThemeColor = currentMainThemeColor.color,
            onModeChange={ newMode-> selectedMode = newMode},
            modifier = Modifier
                    .align(Alignment.TopCenter)
        )

        TimerComponent(
            isRunning = isRunning,
            currentThemeColor = currentMainThemeColor.color,
            timeLeft = timeLeft,
            initialTime = timerViewModel.currentFullTime ?: timeLeft,
            onStartPause = { timerViewModel.onStartPausePressed(
                timeLeft = timeLeft,
                context = context,
                mode = selectedMode,
            ) },
            onReset = { timerViewModel.onResetPressed(resetTime = currentTime*60L) },
        )
    }
}


@Composable
fun TimerComponent(
    isRunning: Boolean,
    timeLeft: Long,
    initialTime: Long,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    currentThemeColor: Color,
){
    val animatedProgress by animateFloatAsState(
        targetValue = if (initialTime>0) { timeLeft.toFloat() / initialTime.toFloat() } else { 1f },
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ){
        Canvas(modifier = Modifier.fillMaxSize()){
            val strokeWidth = 12.dp.toPx()
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = currentThemeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = TimerViewModel.formatTime(timeLeft),
            style = MaterialTheme.typography.displayLarge,
        )


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStartPause) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

}

@Composable
fun ModeSelectorComponent(
    isRunning: Boolean,
    selectedMode: TimerMode,
    onModeChange: (TimerMode) -> Unit,
    modifier: Modifier = Modifier,
    currentThemeColor: Color
) {
    AnimatedVisibility(
        visible = !isRunning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier=modifier
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, currentThemeColor),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
}