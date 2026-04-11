package com.s1g1.tomatoro.ui.stats

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.database.Session
import com.s1g1.tomatoro.database.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class StatsViewModel(private val sessionRepository: SessionRepository): ViewModel() {

    val allSessions: StateFlow<List<Session>> = sessionRepository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // TODO DAY COMPONENTS
    val _dayOffset = MutableStateFlow(0) // 0 - today... -1 - yesterday...
    val dayLabel: StateFlow<String> = _dayOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(calendar.time).uppercase()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val daySessions: StateFlow<List<Session>> = combine(
        allSessions,
        _dayOffset
    ){ sessions, offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)

        val baseCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }

        val startOfDay = baseCalendar.run{
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            timeInMillis
        }

        val endOfDay = baseCalendar.run{
            add(Calendar.DAY_OF_YEAR, 1)
            timeInMillis
        }

        sessions.filter { it.endTimestamp in startOfDay..endOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun moveDay(delta: Int) {
        if (delta<0 || _dayOffset.value != 0){
            _dayOffset.value += delta
        }
    }

    // TODO WEEK COMPONENTS
    val _weekOffset = MutableStateFlow(0) // 0 - this week -1 - prev week
    val weekLabel: StateFlow<String> = _weekOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, offset)

        val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)

        "WEEK $weekNumber"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val weekSessions: StateFlow<List<Session>> = combine(
        allSessions,
        _weekOffset
    ){ sessions, offset ->
        val baseCalendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, offset)
        }

        val startOfWeek = baseCalendar.run {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val endOfWeek = baseCalendar.run {
            add(Calendar.DAY_OF_YEAR, 7)
            timeInMillis
        }

        sessions.filter { it.endTimestamp in startOfWeek..endOfWeek }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun moveWeek(delta: Int) {
        if (delta<0 || _weekOffset.value != 0){
            _weekOffset.value += delta
        }
    }
}