package com.s1g1.tomatoro.ui.stats

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.database.Session
import com.s1g1.tomatoro.database.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class StatsViewModel(private val sessionRepository: SessionRepository): ViewModel() {

    val allSessions: StateFlow<List<Session>> = sessionRepository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _showAllDisplay = MutableStateFlow(false)
    val showAllDisplay = _showAllDisplay.asStateFlow()

    fun onToggleAllSessionDisplay(){
        _showAllDisplay.value = !_showAllDisplay.value
    }

    fun deleteSessionFromDatabase(session: Session){
        viewModelScope.launch(Dispatchers.IO){
            sessionRepository.deleteSession(session = session)
        }
    }

    // TODO DAY COMPONENTS
    private val dayOffset = MutableStateFlow(0) // 0 - today... -1 - yesterday...
    val dayLabel: StateFlow<String> = dayOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(calendar.time).uppercase()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun moveDay(delta: Int) {
        if (delta<0 || dayOffset.value != 0){
            dayOffset.value += delta
        }
    }

    val daySessions: StateFlow<List<Session>> = combine(
        allSessions,
        dayOffset
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
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val endOfDay = baseCalendar.run{
            add(Calendar.DAY_OF_YEAR, 1)
            timeInMillis
        }

        sessions.filter { it.endTimestamp in startOfDay until endOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO WEEK COMPONENTS
    private val weekOffset = MutableStateFlow(0) // 0 - this week -1 - prev week
    val weekLabel: StateFlow<String> = weekOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, offset)

        val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)

        "WEEK $weekNumber"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun moveWeek(delta: Int) {
        if (delta<0 || weekOffset.value != 0){
            weekOffset.value += delta
        }
    }

    val weekSessions: StateFlow<List<Session>> = combine(
        allSessions,
        weekOffset
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

        sessions.filter { it.endTimestamp in startOfWeek until endOfWeek }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO MONTH COMPONENTS
    private val monthOffset = MutableStateFlow(0) // 0 - this month -1 - prev month
    val monthLabel: StateFlow<String> = monthOffset.map { offset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)

        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        sdf.format(calendar.time).uppercase()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun moveMonth(delta: Int) {
        if (delta<0 || monthOffset.value != 0){
            monthOffset.value += delta
        }
    }

    val monthSessions: StateFlow<List<Session>> = combine(
        allSessions,
        monthOffset
    ){ sessions, offset ->
        val baseCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, offset)
        }

        val startOfMonth = baseCalendar.run {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val endOfMonth = baseCalendar.run {
            add(Calendar.MONTH, 1)
            timeInMillis
        }

        sessions.filter { it.endTimestamp in startOfMonth until endOfMonth }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO YEAR COMPONENTS
    private val yearOffset = MutableStateFlow(0) // 0 - this year -1 - prev year
    val yearLabel: StateFlow<String> = yearOffset.map { offset ->
        val calendar = Calendar.getInstance().apply{
            add(Calendar.YEAR, offset)
        }

        calendar.get(Calendar.YEAR).toString()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun moveYear(delta: Int) {
        if (delta<0 || yearOffset.value != 0){
            yearOffset.value += delta
        }
    }

    val yearSessions: StateFlow<List<Session>> = combine(
        allSessions,
        yearOffset
    ){ sessions, offset ->
        val baseCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
            add(Calendar.YEAR, offset)
        }

        val startOfYear = baseCalendar.run {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

        val endOfYear = baseCalendar.run {
            add(Calendar.YEAR, 1)
            timeInMillis
        }

        sessions.filter { it.endTimestamp in startOfYear until endOfYear }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


}