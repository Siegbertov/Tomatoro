package com.s1g1.tomatoro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM $SESSION_TABLE_NAME ORDER BY endTimestamp DESC")
    fun getAllSessions() : Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)
}