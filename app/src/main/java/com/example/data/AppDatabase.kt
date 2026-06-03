package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TableInfo::class, SessionLog::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tableDao(): TableDao
}
