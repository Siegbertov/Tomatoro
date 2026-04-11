package com.s1g1.tomatoro.database

import androidx.room.TypeConverter
import com.s1g1.tomatoro.TimerMode

class SessionConverters {

    /* TimerMode */
    @TypeConverter
    fun fromTimerMode(timerMode: TimerMode): String =  timerMode.name
    @TypeConverter
    fun toTimerMode(modeName: String): TimerMode = TimerMode.fromName(name=modeName)

}