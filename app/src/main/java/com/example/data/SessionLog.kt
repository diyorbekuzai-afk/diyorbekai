package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableId: Int,
    val tableName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val totalDurationMs: Long,
    val price: Long,
    val playMode: PlayMode,
    val statusLabel: String // "COMPLETED", "FREEPLAY", "CANCELLED"
)
