package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM tables ORDER BY id ASC")
    fun getAllTables(): Flow<List<TableInfo>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialTables(tables: List<TableInfo>)

    @Update
    suspend fun updateTable(table: TableInfo)

    @Update
    suspend fun updateTables(tables: List<TableInfo>)

    @Query("SELECT * FROM tables WHERE id = :id LIMIT 1")
    suspend fun getTableById(id: Int): TableInfo?

    @Query("SELECT * FROM session_logs ORDER BY startTimeMs DESC")
    fun getAllLogs(): Flow<List<SessionLog>>

    @Insert
    suspend fun insertLog(log: SessionLog)

    @Query("DELETE FROM session_logs WHERE id NOT IN (SELECT id FROM session_logs ORDER BY startTimeMs DESC LIMIT 200)")
    suspend fun deleteOldLogs()
    
    @Query("DELETE FROM session_logs")
    suspend fun clearLogs()
}
