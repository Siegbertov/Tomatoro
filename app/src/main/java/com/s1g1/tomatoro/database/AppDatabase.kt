package com.s1g1.tomatoro.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.s1g1.tomatoro.database.sessions.NEW_SESSION_TABLE_NAME
import com.s1g1.tomatoro.database.sessions.SESSION_TABLE_NAME
import com.s1g1.tomatoro.database.sessions.Session
import com.s1g1.tomatoro.database.sessions.SessionConverters
import com.s1g1.tomatoro.database.sessions.SessionDao
import com.s1g1.tomatoro.database.tags.TAG_TABLE_NAME
import com.s1g1.tomatoro.database.tags.Tag
import com.s1g1.tomatoro.database.tags.TagDao

@Database(entities = [Session::class, Tag::class], version=3)
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // TODO -1- creates new DB
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $NEW_SESSION_TABLE_NAME (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `endTimestamp` INTEGER NOT NULL, 
                `mode` TEXT NOT NULL, 
                `duration` INTEGER NOT NULL, 
                `tagId` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`tagId`) REFERENCES $TAG_TABLE_NAME (`id`) ON UPDATE NO ACTION ON DELETE SET DEFAULT 
            )
        """.trimIndent())

        // TODO -2- copying data from old to new
        db.execSQL("""
            INSERT INTO $NEW_SESSION_TABLE_NAME (id, endTimestamp, mode, duration)
            SELECT id, endTimestamp, mode, duration FROM $SESSION_TABLE_NAME
        """)

        // TODO -3- delete indexes
        db.execSQL("DROP INDEX IF EXISTS index_${SESSION_TABLE_NAME}_tagId")

        // TODO -4- delete old table
        db.execSQL("DROP TABLE IF EXISTS $TAG_TABLE_NAME")

        // TODO -5- rename new table name to old table name
        db.execSQL("ALTER TABLE $NEW_SESSION_TABLE_NAME RENAME TO $SESSION_TABLE_NAME")

        //TODO -6- creates index for foreign key
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_${SESSION_TABLE_NAME}_tagId` ON $SESSION_TABLE_NAME (`tagId`)")
    }
}
