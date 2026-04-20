package com.s1g1.tomatoro.database.tags

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM $TAG_TABLE_NAME WHERE isHidden=0")
    fun getAllUnhiddenTags() : Flow<List<Tag>>

    @Query("SELECT * FROM $TAG_TABLE_NAME WHERE isHidden=1")
    fun getAllHiddenTags() : Flow<List<Tag>>

    @Query("SELECT * FROM $TAG_TABLE_NAME")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: Tag)

    @Query("DELETE FROM $TAG_TABLE_NAME WHERE id=:tagId AND isRemovable=1")
    suspend fun deleteTagById(tagId: Int)

}