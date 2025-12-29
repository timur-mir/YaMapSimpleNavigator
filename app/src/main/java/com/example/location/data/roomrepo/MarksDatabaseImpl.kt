package com.example.location.data.roomrepo

import android.content.Context
import androidx.room.Room

object MarksDatabaseImpl {
    var INSTANCE: MarksDatabase? = null
        private set

    fun initDatabase(context: Context) {
        if (INSTANCE == null) {
            synchronized(MarksDatabase::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        MarksDatabase::class.java,
                        MarksDatabase.DB_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                }
            }
        }
    }
}