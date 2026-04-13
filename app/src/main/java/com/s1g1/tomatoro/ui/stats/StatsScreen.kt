package com.s1g1.tomatoro.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.Session

@Composable
fun StatsScreen(
    navController: NavHostController,
    statsViewModel: StatsViewModel
) {
    val scrollState = rememberScrollState()

    val dayLabel by statsViewModel.dayLabel.collectAsState()
    val daySessions by statsViewModel.daySessions.collectAsState()

    val weekLabel by statsViewModel.weekLabel.collectAsState()
    val weekSessions by statsViewModel.weekSessions.collectAsState()

    val monthLabel by statsViewModel.monthLabel.collectAsState()
    val monthSessions by statsViewModel.monthSessions.collectAsState()

    val yearLabel by statsViewModel.yearLabel.collectAsState()
    val yearSessions by statsViewModel.yearSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        StatsRow(
            periodLabel = dayLabel,
            sessions = daySessions,
            onLeftClick = { statsViewModel.moveDay(delta = -1) },
            onRightClick = { statsViewModel.moveDay(delta = 1) }
        )

        StatsRow(
            periodLabel = weekLabel,
            sessions = weekSessions,
            onLeftClick = { statsViewModel.moveWeek(delta = -1) },
            onRightClick = { statsViewModel.moveWeek(delta = 1) }
        )

        StatsRow(
            periodLabel = monthLabel,
            sessions = monthSessions,
            onLeftClick = { statsViewModel.moveMonth(delta = -1) },
            onRightClick = { statsViewModel.moveMonth(delta = 1) }
        )

        StatsRow(
            periodLabel = yearLabel,
            sessions = yearSessions,
            onLeftClick = { statsViewModel.moveYear(delta = -1) },
            onRightClick = { statsViewModel.moveYear(delta = 1) }
        )
    }
}

@Composable
fun StatsRow(
    periodLabel: String,
    sessions: List<Session>,
    onLeftClick: ()->Unit,
    onRightClick: ()->Unit,
){

    val totalTime = sessions.sumOf { it.duration }

    val tomatoroPercentage = if (totalTime!=0L) {
        sessions.sumOf { if(it.mode== TimerMode.TOMATORO) it.duration else 0 } * 100 / totalTime
    } else 0
    val breakPercentage = if (totalTime!=0L) {
        sessions.sumOf { if(it.mode== TimerMode.BREAK) it.duration else 0 } * 100 / totalTime
    } else 0
    val longBreakPercentage = if (totalTime!=0L) {
        sessions.sumOf { if(it.mode== TimerMode.LONG_BREAK) it.duration else 0 } * 100 / totalTime
    } else 0

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLeftClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }
            Text(text = periodLabel)
            IconButton(onClick = onRightClick) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(10.dp)
                ){
                    // OUTLINE
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.25f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    if (totalTime == 0L){

                        // EMPTY CIRCLE
                        drawCircle(
                            color = Color.White,
                            radius = size.minDimension / 2,
                        )
                    } else {
                        val breakAngle = breakPercentage * 3.6f
                        val longBreakAngle = longBreakPercentage * 3.6f
                        val tomatoroAngle = 360f - breakAngle - longBreakAngle
                        var currentStartAngle = -90f

                        listOf(
                            TimerMode.BREAK.color to breakAngle,
                            TimerMode.LONG_BREAK.color to longBreakAngle,
                            TimerMode.TOMATORO.color to tomatoroAngle
                        ).forEach { (color, angle) ->
                            drawArc(
                                color = color,
                                startAngle = currentStartAngle,
                                sweepAngle = angle,
                                useCenter = true
                            )
                            currentStartAngle += angle
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = "TOTAL: ${(totalTime/60).toInt()} mins",
                        fontWeight = FontWeight.Black
                    )
                    if(totalTime!=0L){
                        listOf(
                            tomatoroPercentage to TimerMode.TOMATORO,
                            breakPercentage to TimerMode.BREAK,
                            longBreakPercentage to TimerMode.LONG_BREAK
                        ).forEach { (percentage, mode) ->
                            Text(
                                text = "${stringResource(mode.title)}: $percentage%",
                                color = mode.color,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}