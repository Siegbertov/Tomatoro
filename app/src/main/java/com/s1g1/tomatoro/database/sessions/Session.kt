package com.s1g1.tomatoro.database.sessions

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.s1g1.tomatoro.TimerMode

const val SESSION_TABLE_NAME = "sessions"
@Entity(tableName = SESSION_TABLE_NAME)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val endTimestamp: Long,
    val mode: TimerMode,
    val duration: Long,
)
