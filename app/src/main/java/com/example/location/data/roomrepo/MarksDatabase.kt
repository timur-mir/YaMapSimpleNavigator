package com.example.location.data.roomrepo

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.location.data.roomrepo.MarksDatabase.Companion.DB_VERSION


@Database(
    entities = [MarkEntity::class],
    version = DB_VERSION
)

abstract class MarksDatabase: RoomDatabase() {
    abstract fun getMarksDao(): MarksDao

    companion object {
        const val DB_VERSION = 3
        const val DB_NAME = "MarksBase"
    }
}