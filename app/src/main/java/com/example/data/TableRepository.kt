package com.example.data

import kotlinx.coroutines.flow.Flow

class TableRepository(private val dao: TableDao) {
    val allTables: Flow<List<TableInfo>> = dao.getAllTables()
    val allLogs: Flow<List<SessionLog>> = dao.getAllLogs()

    suspend fun initializeTables() {
        val initialTables = (1..5).map { id ->
            TableInfo(
                id = id,
                name = "Stol $id",
                status = TableStatus.EMPTY,
                defaultDurationMs = 60 * 60 * 1000L, // 1 hour
                playMode = PlayMode.COUNTDOWN,
                activeStartTimeMs = 0L,
                accumulatedTimeMs = 0L,
                targetDurationMs = 0L,
                historyStartTimeMs = 0L
            )
        }
        dao.insertInitialTables(initialTables)
    }

    suspend fun updateTable(table: TableInfo) {
        dao.updateTable(table)
    }

    suspend fun updateTables(tables: List<TableInfo>) {
        dao.updateTables(tables)
    }

    suspend fun logSession(log: SessionLog) {
        dao.insertLog(log)
        dao.deleteOldLogs()
    }
}
