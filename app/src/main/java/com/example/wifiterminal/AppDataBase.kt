package com.example.wifiterminal

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [TerminalEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun terminalDao(): TerminalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "terminal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}