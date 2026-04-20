package com.s1g1.tomatoro.database.tags

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TAG_TABLE_NAME = "tags"
@Entity(tableName = TAG_TABLE_NAME)
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isRemovable: Boolean = false, // default value for NONE, WORK, STUDY
    val isHidden: Boolean = false, //  by default for all new TAGS
)

// TODO HARD DELETE -
//  delete tag
//  ===> all sessions that had reference to this id in tagId - changes to 0 - for NONE

// TODO HARD DELETE -
//  hide tag
//  ===> isHidden=true

//db.execSQL(
//"INSERT INTO tags (id, title, isRemovable, isHidden) VALUES (0, 'NONE', 0, 0)"
//)

//db.execSQL(
//"INSERT INTO tags (id, title, isRemovable, isHidden) VALUES (1, 'WORK', 0, 0)"
//)

//db.execSQL(
//"INSERT INTO tags (id, title, isRemovable, isHidden) VALUES (2, 'STUDY', 0, 0)"
//)