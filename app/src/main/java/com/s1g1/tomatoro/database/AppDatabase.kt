package com.s1g1.tomatoro.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.s1g1.tomatoro.database.sessions.Session
import com.s1g1.tomatoro.database.sessions.SessionConverters
import com.s1g1.tomatoro.database.sessions.SessionDao
import com.s1g1.tomatoro.database.tags.TAG_TABLE_NAME
import com.s1g1.tomatoro.database.tags.Tag
import com.s1g1.tomatoro.database.tags.TagDao

@Database(entities = [Session::class, Tag::class], version=2)
@TypeConverters(SessionConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun tagDao(): TagDao

}

val TAGS_PREFILL_CALLBACK = object : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (0, 'NONE', 0, 0)")
        db.execSQL("INSERT INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (1, 'WORK', 0, 0)")
        db.execSQL("INSERT INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (2, 'STUDY', 0, 0)")
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("INSERT OR IGNORE INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (0, 'NONE', 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (1, 'WORK', 0, 0)")
        db.execSQL("INSERT OR IGNORE INTO $TAG_TABLE_NAME (id, title, isRemovable, isHidden) VALUES (2, 'STUDY', 0, 0)")
    }

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TAG_TABLE_NAME (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `title` TEXT NOT NULL, 
                `isRemovable` INTEGER NOT NULL DEFAULT '0', 
                `isHidden` INTEGER NOT NULL DEFAULT '0'
            )
        """.trimIndent())
    }
}