package com.example.wifiterminal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TerminalDao {
    @Insert
    suspend fun insert(entry: TerminalEntry)

    @Query("SELECT * FROM terminal_entries ORDER BY timestamp DESC")
    suspend fun getAllEntries(): List<TerminalEntry>
}