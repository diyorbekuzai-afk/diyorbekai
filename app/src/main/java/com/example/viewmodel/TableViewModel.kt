package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.receiver.AlarmScheduler
import com.example.utils.PriceCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TableViewModel(
    private val repository: TableRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val allLogs: Flow<List<SessionLog>> = repository.allLogs

    val tables: StateFlow<List<TableInfo>> = repository.allTables.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentTimeSeconds = MutableStateFlow(System.currentTimeMillis() / 1000)
    val currentTimeSeconds: StateFlow<Long> = _currentTimeSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val current = repository.allTables.first()
            if (current.isEmpty()) {
                repository.initializeTables()
            }
        }
        startClock()
    }

    private fun startClock() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _currentTimeSeconds.update { System.currentTimeMillis() / 1000 }
                delay(1000)
            }
        }
    }

    fun startTableCountdown(table: TableInfo, durationMs: Long = table.defaultDurationMs) {
        val now = System.currentTimeMillis()
        val updated = table.copy(
            status = TableStatus.RUNNING,
            playMode = PlayMode.COUNTDOWN,
            activeStartTimeMs = now,
            accumulatedTimeMs = 0L,
            targetDurationMs = durationMs,
            historyStartTimeMs = now
        )
        alarmScheduler.scheduleAlarm(table.id, table.name, now + durationMs)
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun startTableFreePlay(table: TableInfo) {
        val now = System.currentTimeMillis()
        val updated = table.copy(
            status = TableStatus.RUNNING,
            playMode = PlayMode.FREE_PLAY,
            activeStartTimeMs = now,
            accumulatedTimeMs = 0L,
            targetDurationMs = 0L,
            historyStartTimeMs = now
        )
        alarmScheduler.cancelAlarm(table.id)
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun startTableCustom(table: TableInfo, pastMinutes: Long, isFreePlay: Boolean) {
        val now = System.currentTimeMillis()
        val pastMs = pastMinutes * 60_000L
        
        val updated = if (isFreePlay) {
            table.copy(
                status = TableStatus.RUNNING,
                playMode = PlayMode.FREE_PLAY,
                activeStartTimeMs = now - pastMs,
                accumulatedTimeMs = 0L,
                targetDurationMs = 0L,
                historyStartTimeMs = now - pastMs
            )
        } else {
            table.copy(
                status = TableStatus.RUNNING,
                playMode = PlayMode.COUNTDOWN,
                activeStartTimeMs = now - pastMs,
                accumulatedTimeMs = 0L,
                targetDurationMs = table.defaultDurationMs,
                historyStartTimeMs = now - pastMs
            )
        }
        
        if (!isFreePlay) {
            val remaining = table.defaultDurationMs - pastMs
            if (remaining > 0) {
                alarmScheduler.scheduleAlarm(table.id, table.name, now + remaining)
            } else {
                alarmScheduler.cancelAlarm(table.id)
            }
        } else {
            alarmScheduler.cancelAlarm(table.id)
        }
        
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun pauseTable(table: TableInfo) {
        if (table.status != TableStatus.RUNNING) return
        val now = System.currentTimeMillis()
        val accumulated = table.accumulatedTimeMs + (now - table.activeStartTimeMs)
        val updated = table.copy(
            status = TableStatus.PAUSED,
            accumulatedTimeMs = accumulated
        )
        alarmScheduler.cancelAlarm(table.id)
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun resumeTable(table: TableInfo) {
        if (table.status != TableStatus.PAUSED) return
        val now = System.currentTimeMillis()
        val updated = table.copy(
            status = TableStatus.RUNNING,
            activeStartTimeMs = now
        )
        if (table.playMode == PlayMode.COUNTDOWN) {
            val remaining = table.targetDurationMs - table.accumulatedTimeMs
            if (remaining > 0) {
                alarmScheduler.scheduleAlarm(table.id, table.name, now + remaining)
            }
        }
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun resetTable(table: TableInfo) {
        val now = System.currentTimeMillis()
        val elapsed = if (table.status == TableStatus.RUNNING) {
            table.accumulatedTimeMs + (now - table.activeStartTimeMs)
        } else {
            table.accumulatedTimeMs
        }
        
        if (elapsed > 0 && table.historyStartTimeMs > 0) {
            if (elapsed >= 300_000L) { // 5-minute protection rule
                val price = PriceCalculator.calculatePrice(elapsed)
                
                val statusLabel = when (table.playMode) {
                    PlayMode.FREE_PLAY -> "FREEPLAY"
                    PlayMode.COUNTDOWN -> if (elapsed >= table.targetDurationMs) "COMPLETED" else "CANCELLED"
                }

                val log = SessionLog(
                    tableId = table.id,
                    tableName = table.name,
                    startTimeMs = table.historyStartTimeMs,
                    endTimeMs = now,
                    totalDurationMs = elapsed,
                    price = price,
                    playMode = table.playMode,
                    statusLabel = statusLabel
                )
                viewModelScope.launch { repository.logSession(log) }
            }
        }

        alarmScheduler.cancelAlarm(table.id)
        val updated = table.copy(
            status = TableStatus.EMPTY,
            accumulatedTimeMs = 0L,
            activeStartTimeMs = 0L,
            targetDurationMs = 0L,
            historyStartTimeMs = 0L,
            playMode = PlayMode.COUNTDOWN
        )
        viewModelScope.launch { repository.updateTable(updated) }
    }

    fun addTimeToTable(tableId: Int, extraMs: Long) {
        viewModelScope.launch {
            val tablesList = repository.allTables.first()
            val table = tablesList.find { it.id == tableId } ?: return@launch
            
            if (table.status == TableStatus.EMPTY) {
                val updated = table.copy(defaultDurationMs = extraMs)
                repository.updateTable(updated)
            } else if (table.playMode == PlayMode.COUNTDOWN) {
                val updated = table.copy(
                    targetDurationMs = table.targetDurationMs + extraMs
                )
                if (table.status == TableStatus.RUNNING) {
                    val elapsed = table.accumulatedTimeMs + (System.currentTimeMillis() - table.activeStartTimeMs)
                    val remaining = updated.targetDurationMs - elapsed
                    alarmScheduler.cancelAlarm(table.id)
                    if (remaining > 0) {
                        alarmScheduler.scheduleAlarm(table.id, table.name, System.currentTimeMillis() + remaining)
                    }
                }
                repository.updateTable(updated)
            }
        }
    }
    
    fun changeTableName(tableId: Int, newName: String) {
        viewModelScope.launch {
            val tablesList = repository.allTables.first()
            val table = tablesList.find { it.id == tableId } ?: return@launch
            val updated = table.copy(name = newName)
            repository.updateTable(updated)
        }
    }

    fun startAll() {
        val currentTables = tables.value
        currentTables.forEach { table ->
            if (table.status == TableStatus.EMPTY) {
                startTableCountdown(table)
            } else if (table.status == TableStatus.PAUSED) {
                resumeTable(table)
            }
        }
    }

    fun pauseAll() {
        val currentTables = tables.value
        currentTables.forEach { table ->
            if (table.status == TableStatus.RUNNING) {
                pauseTable(table)
            }
        }
    }

    fun resetAll() {
        val currentTables = tables.value
        currentTables.forEach { table ->
            if (table.status != TableStatus.EMPTY) {
                resetTable(table)
            }
        }
    }
    
    fun transferTable(fromTable: TableInfo, toTable: TableInfo) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            
            // toTable becomes exactly what fromTable was, but keeps its own id, name, and defaultDurationMs
            val newToTable = toTable.copy(
                status = fromTable.status,
                playMode = fromTable.playMode,
                activeStartTimeMs = fromTable.activeStartTimeMs,
                accumulatedTimeMs = fromTable.accumulatedTimeMs,
                targetDurationMs = fromTable.targetDurationMs,
                historyStartTimeMs = fromTable.historyStartTimeMs
            )
            
            // fromTable resets to empty
            val newFromTable = fromTable.copy(
                status = TableStatus.EMPTY,
                accumulatedTimeMs = 0L,
                activeStartTimeMs = 0L,
                targetDurationMs = 0L,
                historyStartTimeMs = 0L,
                playMode = PlayMode.COUNTDOWN
            )

            // cancel alarm for fromTable
            alarmScheduler.cancelAlarm(fromTable.id)
            
            // set alarm for toTable if it was counting down and running
            if (newToTable.status == TableStatus.RUNNING && newToTable.playMode == PlayMode.COUNTDOWN) {
                val elapsed = newToTable.accumulatedTimeMs + (now - newToTable.activeStartTimeMs)
                val remaining = newToTable.targetDurationMs - elapsed
                if (remaining > 0) {
                    alarmScheduler.scheduleAlarm(newToTable.id, newToTable.name, now + remaining)
                }
            }

            repository.updateTables(listOf(newFromTable, newToTable))
        }
    }
}

class TableViewModelFactory(
    private val repository: TableRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TableViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
