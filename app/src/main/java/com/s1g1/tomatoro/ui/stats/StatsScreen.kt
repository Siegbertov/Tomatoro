package com.s1g1.tomatoro.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.Session

@Composable
fun StatsScreen(
    navController: NavHostController,
    statsViewModel: StatsViewModel
) {
    val allSessions by statsViewModel.allSessions.collectAsState()

    val daySessions by statsViewModel.daySessions.collectAsState()
    val dayLabel by statsViewModel.dayLabel.collectAsState()

    val weekSessions by statsViewModel.weekSessions.collectAsState()
    val weekLabel by statsViewModel.weekLabel.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        StatsRow(
            periodTitle = dayLabel,
            sessions = daySessions,
            onLeftClick = { statsViewModel.moveDay(delta = -1) },
            onRightClick = { statsViewModel.moveDay(delta = 1) }
        )

        StatsRow(
            periodTitle = weekLabel,
            sessions = weekSessions,
            onLeftClick = { statsViewModel.moveWeek(delta = -1) },
            onRightClick = { statsViewModel.moveWeek(delta = 1) }
        )
    }
}

@Composable
fun StatsRow(
    periodTitle: String,
    sessions: List<Session>,
    onLeftClick: ()->Unit,
    onRightClick: ()->Unit,
){
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLeftClick) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
            }
            Text(text = periodTitle)
            IconButton(onClick = onRightClick) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            val totalTime = sessions.sumOf { it.duration }
            val totalTomatoro = sessions.sumOf { if(it.mode== TimerMode.TOMATORO) it.duration else 0 }
            val totalBreak = sessions.sumOf { if(it.mode== TimerMode.BREAK) it.duration else 0 }
            val totalLongBreak = sessions.sumOf { if(it.mode== TimerMode.LONG_BREAK) it.duration else 0 }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(text = "TOTAL: ${(totalTime/60).toInt()} mins")
                Text(text = "TOTAL TOMATORO: ${(totalTomatoro/60).toInt()} mins")
                Text(text = "TOTAL BREAK: ${(totalBreak/60).toInt()} mins")
                Text(text = "TOTAL LONG BREAK: ${(totalLongBreak/60).toInt()} mins")

            }
        }
    }

}