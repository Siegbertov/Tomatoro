package com.s1g1.tomatoro.database.sessions

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM $SESSION_TABLE_NAME ORDER BY endTimestamp DESC")
    fun getAllSessionsWithTags() : Flow<List<SessionWithTag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)
}