package com.example.wifiterminal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "terminal_entries")
data class TerminalEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val content: String
)
