package com.s1g1.tomatoro.database.sessions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.tags.Tag

const val SESSION_TABLE_NAME = "sessions"
const val NEW_SESSION_TABLE_NAME = "new_sessions"
@Entity(
    tableName = SESSION_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val endTimestamp: Long,
    val mode: TimerMode,
    val duration: Long,
    @ColumnInfo(defaultValue = "0") val tagId: Int = 0,
)
