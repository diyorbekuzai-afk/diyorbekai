package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TableStatus {
    EMPTY, RUNNING, PAUSED
}

enum class PlayMode {
    COUNTDOWN, FREE_PLAY
}

@Entity(tableName = "tables")
data class TableInfo(
    @PrimaryKey val id: Int,
    val name: String,
    val status: TableStatus,
    val defaultDurationMs: Long,
    
    val playMode: PlayMode,
    val activeStartTimeMs: Long,
    val accumulatedTimeMs: Long,
    val targetDurationMs: Long,
    val historyStartTimeMs: Long
)
