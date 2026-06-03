package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.data.PlayMode
import com.example.data.TableInfo
import com.example.data.TableStatus
import com.example.ui.theme.*
import com.example.viewmodel.TableViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TableViewModel) {
    val tables by viewModel.tables.collectAsState()
    val currentTimeSeconds by viewModel.currentTimeSeconds.collectAsState()

    var showCustomStartDialog by remember { mutableStateOf<TableInfo?>(null) }
    var showAddTimeDialog by remember { mutableStateOf<TableInfo?>(null) }
    var showTransferDialog by remember { mutableStateOf<TableInfo?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsTennis, contentDescription = null, tint = Rose500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stol Tennis Timer",
                        color = MainTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .background(Emerald600.copy(alpha = 0.2f), RoundedCornerShape(100))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTimeSeconds * 1000)),
                        color = Emerald500,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.startAll() },
                    border = BorderStroke(1.dp, Emerald600.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald500, containerColor = Emerald600.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Hammasini boshlash", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.pauseAll() },
                    border = BorderStroke(1.dp, Indigo600.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Indigo500, containerColor = Indigo600.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Hammasini pauza", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.resetAll() },
                    border = BorderStroke(1.dp, Rose500.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose400, containerColor = Rose500.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Hammasini reset", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(tables) { table ->
                    TableCard(
                        table = table,
                        currentTimeSeconds = currentTimeSeconds,
                        onStart = { viewModel.startTableCountdown(table) },
                        onStartFree = { viewModel.startTableFreePlay(table) },
                        onPause = { viewModel.pauseTable(table) },
                        onResume = { viewModel.resumeTable(table) },
                        onReset = { viewModel.resetTable(table) },
                        onCustomStart = { showCustomStartDialog = table },
                        onAddTime = { showAddTimeDialog = table },
                        onTransferClick = { showTransferDialog = table }
                    )
                }
            }
        }
    }
    
    if (showCustomStartDialog != null) {
        CustomStartDialog(
            table = showCustomStartDialog!!,
            onDismiss = { showCustomStartDialog = null },
            onConfirm = { mins, isFreePlay ->
                viewModel.startTableCustom(showCustomStartDialog!!, mins, isFreePlay)
                showCustomStartDialog = null
            }
        )
    }

    if (showAddTimeDialog != null) {
        TimeDialog(
            table = showAddTimeDialog!!,
            onDismiss = { showAddTimeDialog = null },
            onConfirm = { extraMs ->
                viewModel.addTimeToTable(showAddTimeDialog!!.id, extraMs)
                showAddTimeDialog = null
            }
        )
    }

    if (showTransferDialog != null) {
        val availableTables = tables.filter { it.status == TableStatus.EMPTY }
        TransferDialog(
            fromTable = showTransferDialog!!,
            availableTables = availableTables,
            onDismiss = { showTransferDialog = null },
            onConfirm = { toTable ->
                viewModel.transferTable(showTransferDialog!!, toTable)
                showTransferDialog = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCard(
    table: TableInfo,
    currentTimeSeconds: Long,
    onStart: () -> Unit,
    onStartFree: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onCustomStart: () -> Unit,
    onAddTime: () -> Unit,
    onTransferClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val nowMs = currentTimeSeconds * 1000L
    
    val elapsedMs = if (table.status == TableStatus.RUNNING) {
        table.accumulatedTimeMs + (nowMs - table.activeStartTimeMs)
    } else {
        table.accumulatedTimeMs
    }

    val displayString: String
    val isOvertime: Boolean
    val isLowTime: Boolean

    if (table.playMode == PlayMode.COUNTDOWN) {
        val remaining = table.targetDurationMs - elapsedMs
        if (remaining > 0) {
            displayString = formatTime(remaining)
            isOvertime = false
            isLowTime = remaining <= 5 * 60 * 1000
        } else {
            displayString = "+ " + formatTime(-remaining)
            isOvertime = true
            isLowTime = false
        }
    } else {
        displayString = formatTime(elapsedMs)
        isOvertime = false
        isLowTime = false
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val price = com.example.utils.PriceCalculator.calculatePrice(elapsedMs)

    val dotColor = when (table.status) {
        TableStatus.EMPTY -> StatusEmptyColor
        TableStatus.RUNNING -> if (isOvertime) Rose500 else Emerald500
        TableStatus.PAUSED -> Amber500
    }
    
    val isFreePlayActive = table.playMode == PlayMode.FREE_PLAY && table.status != TableStatus.EMPTY
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(
                if (isOvertime) 2.dp else 1.dp,
                if (isOvertime) Rose500.copy(alpha = pulseAlpha) else if (isFreePlayActive) Indigo500 else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (table.status != TableStatus.EMPTY) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTransferClick()
                    }
                }
            ),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(if (isFreePlayActive) Indigo500 else dotColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = table.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainTextColor
                    )
                }
                
                if (isFreePlayActive) {
                    Surface(
                        color = StatusEmptyColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Erkin o'yin",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else if (table.status != TableStatus.EMPTY) {
                    Surface(
                        color = StatusEmptyColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = formatTimeCompact(table.defaultDurationMs),
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Display
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFreePlayActive) {
                        Icon(Icons.Default.ArrowDropUp, contentDescription = null, tint = Indigo500, modifier = Modifier.size(36.dp))
                    }
                    Text(
                        text = displayString,
                        fontSize = 48.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (isFreePlayActive) Indigo500 else if (isOvertime) Rose500 else if (isLowTime) Amber500 else Slate300,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            // Price Display
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                    val priceStr = formatter.format(price)
                    Text(text = "$priceStr so'm", color = Emerald500, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Tag
            if (table.status == TableStatus.EMPTY) {
                Surface(
                    color = StatusEmptyColor.copy(alpha=0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "BO'SH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusEmptyTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFreePlayActive) {
                        Surface(
                            color = Indigo500.copy(alpha=0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(Indigo500, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ERKIN O'YIN", color = Indigo500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isOvertime) {
                        Surface(
                            color = Rose500.copy(alpha=0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(Rose500, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                val origTime = formatTimeCompact(table.defaultDurationMs).uppercase(Locale.getDefault())
                                Text("$origTime TUGADI. QO'SHIMCHA KETMOQDA!", color = Rose500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                   
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = table.activeStartTimeMs
                    val startTime = String.format(Locale.getDefault(), "%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                    Text("Boshlangan: $startTime", color = Slate400, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Buttons matrix
            if (table.status == TableStatus.EMPTY) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }
                    Button(
                        onClick = onStartFree,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = Indigo500)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Erkin", color = Indigo500)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCustomStart,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HistoryToggleOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("O'tgan vaqt", color = Slate400, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAddTime,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vaqt", color = Slate400, fontSize = 12.sp)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (table.status == TableStatus.RUNNING) {
                        Button(
                            onClick = onPause,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600.copy(alpha=0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp), tint = Amber500)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pauza", color = Amber500)
                        }
                    } else if (table.status == TableStatus.PAUSED) {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600.copy(alpha=0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald500)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Davom etish", color = Emerald500)
                        }
                    }
                    
                    Button(
                        onClick = onTransferClick,
                        modifier = Modifier.width(56.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = Indigo500)
                    }
                    
                    Button(
                        onClick = onReset,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFreePlayActive) Indigo600.copy(alpha=0.2f) else Rose600.copy(alpha=0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isFreePlayActive) Indigo500 else Rose400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tugatish", color = if (isFreePlayActive) Indigo500 else Rose400)
                    }
                }
                
                if (table.playMode == PlayMode.COUNTDOWN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddTime,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate400)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Vaqt qo'shish", color = Slate400)
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun formatTimeCompact(ms: Long): String {
    val mins = ms / 60000
    if (mins >= 60 && mins % 60 == 0L) return "${mins/60} soat"
    return "$mins minut"
}

@Composable
fun CustomStartDialog(table: TableInfo, onDismiss: () -> Unit, onConfirm: (Long, Boolean) -> Unit) {
    var minInput by remember { mutableStateOf("") }
    var selectedTimeStr by remember { mutableStateOf("-- : --") }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕰", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Maxsus Boshlash", color = MainTextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${table.name} uchun", color = Slate400, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Section 1: Minutes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBgColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Qancha vaqt o'tib ketgan? (minut)", color = Slate400, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PresetButton("+5 min", Modifier.weight(1f)) { minInput = "5"; selectedTimeStr = "-- : --" }
                            PresetButton("+10 min", Modifier.weight(1f)) { minInput = "10"; selectedTimeStr = "-- : --" }
                            PresetButton("+15 min", Modifier.weight(1f)) { minInput = "15"; selectedTimeStr = "-- : --" }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = minInput,
                            onValueChange = { minInput = it; selectedTimeStr = "-- : --" },
                            placeholder = { Text("Masalan: 12", color = Slate600) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MainTextColor,
                                unfocusedTextColor = MainTextColor,
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = Slate600,
                                focusedContainerColor = StatusEmptyColor,
                                unfocusedContainerColor = StatusEmptyColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Section 2: Specific Time
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBgColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yoki aniq boshlangan vaqtni tanlang\n(Masalan: 17:30)", color = Slate400, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = selectedTimeStr,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MainTextColor,
                                disabledBorderColor = Slate600,
                                disabledContainerColor = StatusEmptyColor,
                                disabledTrailingIconColor = Slate400
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                val cal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                        minInput = ""
                                    },
                                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                                    cal.get(java.util.Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            trailingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = "Vaqtni tanlash", tint = Slate400)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val pastMin = calculatePastMinutes(minInput, selectedTimeStr)
                            onConfirm(pastMin, false) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBgColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Taymer", color = DarkBgColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Button(
                        onClick = { 
                            val pastMin = calculatePastMinutes(minInput, selectedTimeStr)
                            onConfirm(pastMin, true) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Erkin o'yin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Bekor qilish", color = Slate400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun calculatePastMinutes(minInput: String, selectedTimeStr: String): Long {
    if (minInput.isNotEmpty()) {
        return minInput.toLongOrNull() ?: 0L
    }
    if (selectedTimeStr != "-- : --") {
        try {
            val parts = selectedTimeStr.split(":")
            if (parts.size == 2) {
                val selHour = parts[0].trim().toInt()
                val selMin = parts[1].trim().toInt()
                
                val now = java.util.Calendar.getInstance()
                val nowHour = now.get(java.util.Calendar.HOUR_OF_DAY)
                val nowMin = now.get(java.util.Calendar.MINUTE)
                
                var past = (nowHour * 60 + nowMin) - (selHour * 60 + selMin)
                if (past < 0) {
                    past += 24 * 60
                }
                return past.toLong()
            }
        } catch (e: Exception) {}
    }
    return 0L
}

@Composable
fun TimeDialog(table: TableInfo, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var hours by remember { mutableStateOf(1) }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏰ ", fontSize = 20.sp)
                    Text("Vaqt belgilash", color = MainTextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${table.name} uchun vaqt kiriting", color = Slate400, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Spinners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeSpinner(label = "Soat", value = hours, onValueChange = { hours = it }, max = 23)
                    Text(" : ", color = Slate400, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp))
                    TimeSpinner(label = "Minut", value = minutes, onValueChange = { minutes = it }, max = 59)
                    Text(" : ", color = Slate400, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp))
                    TimeSpinner(label = "Sekund", value = seconds, onValueChange = { seconds = it }, max = 59)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Presets
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetButton("30 min", Modifier.weight(1f)) { hours=0; minutes=30; seconds=0 }
                    PresetButton("45 min", Modifier.weight(1f)) { hours=0; minutes=45; seconds=0 }
                    PresetButton("1 soat", Modifier.weight(1f)) { hours=1; minutes=0; seconds=0 }
                    PresetButton("1.5 soat", Modifier.weight(1f)) { hours=1; minutes=30; seconds=0 }
                }
                Spacer(modifier = Modifier.height(8.dp))
                PresetButton("2 soat", Modifier.fillMaxWidth()) { hours=2; minutes=0; seconds=0 }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Bekor qilish", color = Slate400, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val totalMs = (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L)
                            onConfirm(totalMs)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                         Text("✓ Saqlash", color = DarkBgColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSpinner(label: String, value: Int, onValueChange: (Int) -> Unit, max: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Slate400, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick = { onValueChange(if (value >= max) 0 else value + 1) },
            modifier = Modifier.size(40.dp, 32.dp).background(StatusEmptyColor, RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = Slate300)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp, 56.dp)
                .background(DarkBgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(value.toString(), color = MainTextColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick = { onValueChange(if (value <= 0) max else value - 1) },
            modifier = Modifier.size(40.dp, 32.dp).background(StatusEmptyColor, RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Down", tint = Slate300)
        }
    }
}

@Composable
private fun PresetButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(text, color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransferDialog(
    fromTable: TableInfo,
    availableTables: List<TableInfo>,
    onDismiss: () -> Unit,
    onConfirm: (TableInfo) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Stolni ko'chirish", color = MainTextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${fromTable.name} dan qaysi stolga ko'chiramiz?", color = Slate400, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (availableTables.isEmpty()) {
                    Text("Bo'sh stollar yo'q", color = Rose500, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max=300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTables) { table ->
                            Button(
                                onClick = { onConfirm(table) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(table.name, color = MainTextColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Bekor qilish", color = Slate400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
