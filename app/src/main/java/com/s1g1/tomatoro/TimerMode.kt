package com.s1g1.tomatoro

import androidx.annotation.StringRes

enum class TimerMode(
    @StringRes val title: Int,
    val defaultDuration: Int
){
    TOMATORO(title = R.string.tomatoro_session, defaultDuration = 25),
    BREAK(title = R.string.short_break_session, defaultDuration = 5),
    LONG_BREAK(title = R.string.long_break_session, defaultDuration = 15);

    companion object {
        fun fromName(name: String) : TimerMode{
            return entries.find{it.name == name} ?: getDefault()
        }

        fun getDefault(): TimerMode {
            return TimerMode.TOMATORO
        }
    }
}