package com.s1g1.tomatoro

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

enum class TimerMode(
    @StringRes val title: Int,
    val color: Color,
    val defaultDuration: Int,
){
    TOMATORO(
        title = R.string.tomatoro_session,
        color = Color(0xFFE57373),
        defaultDuration = 25,
    ),
    BREAK(
        title = R.string.short_break_session,
        color = Color(0xFF81C784),
        defaultDuration = 5,
        ),
    LONG_BREAK(
        title = R.string.long_break_session,
        color = Color(0xFF64B5F6),
        defaultDuration = 15,
    );

    companion object {
        fun fromName(name: String) : TimerMode{
            return entries.find{it.name == name} ?: getDefault()
        }

        fun getDefault(): TimerMode {
            return TimerMode.TOMATORO
        }
    }
}