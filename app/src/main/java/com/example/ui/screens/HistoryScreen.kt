package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayMode
import com.example.data.SessionLog
import com.example.ui.theme.*
import com.example.viewmodel.TableViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class TimeFilter {
    TODAY, WEEK, ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TableViewModel
) {
    val allLogs by viewModel.allLogs.collectAsState(initial = emptyList<SessionLog>())
    var currentFilter by remember { mutableStateOf(TimeFilter.TODAY) }
    
    val filteredLogs = remember(allLogs, currentFilter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        
        when (currentFilter) {
            TimeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                allLogs.filter { it.endTimeMs >= startOfDay }
            }
            TimeFilter.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val startOfWeek = cal.timeInMillis
                allLogs.filter { it.endTimeMs >= startOfWeek }
            }
            TimeFilter.ALL -> allLogs
        }
    }
    
    val totalGames = filteredLogs.size
    val totalTimeMs = filteredLogs.sumOf { it.totalDurationMs }
    val totalRevenue = filteredLogs.sumOf { it.price }
    
    val mostActiveTable = remember(filteredLogs) {
        if (filteredLogs.isEmpty()) "Yo'q"
        else {
            val counts = filteredLogs.groupingBy { it.tableName }.eachCount()
            counts.maxByOrNull { it.value }?.key ?: "Yo'q"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O'yin Tarixi", color = MainTextColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    scrolledContainerColor = BgColor
                )
            )
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {
            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "O'yinlar", value = "$totalGames", modifier = Modifier.weight(1f))
                val hours = totalTimeMs / (1000 * 60 * 60)
                val mins = (totalTimeMs % (1000 * 60 * 60)) / (1000 * 60)
                StatCard(title = "Jami vaqt", value = "${hours}s ${mins}d", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val formatter = java.text.NumberFormat.getNumberInstance(Locale.US)
                val totalRevenueStr = formatter.format(totalRevenue)
                StatCard(title = "Daromad", value = "$totalRevenueStr so'm", valueColor = Emerald500, modifier = Modifier.weight(1f))
                StatCard(title = "Eng faol", value = mostActiveTable, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterButton("Bugun", currentFilter == TimeFilter.TODAY, Modifier.weight(1f)) { currentFilter = TimeFilter.TODAY }
                FilterButton("Hafta", currentFilter == TimeFilter.WEEK, Modifier.weight(1f)) { currentFilter = TimeFilter.WEEK }
                FilterButton("Hammasi", currentFilter == TimeFilter.ALL, Modifier.weight(1f)) { currentFilter = TimeFilter.ALL }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    LogItemView(log)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, valueColor: Color = MainTextColor, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Slate400, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Emerald500 else StatusEmptyColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = if (isSelected) Color.White else Slate400, fontSize = 14.sp)
    }
}

@Composable
fun LogItemView(log: SessionLog) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(log.tableName, color = MainTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                val endFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                val timeStr = if (log.startTimeMs > 0) {
                    "${fmt.format(Date(log.startTimeMs))} - ${endFmt.format(Date(log.endTimeMs))}"
                } else endFmt.format(Date(log.endTimeMs))
                
                Text(timeStr, color = Slate400, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val durationMins = (log.totalDurationMs / 60000.0).roundToInt()
                val hours = durationMins / 60
                val mins = durationMins % 60
                val timeText = if (hours > 0) {
                    if (mins > 0) "$hours soat-u $mins daqiqa" else "$hours soat"
                } else {
                    "$mins daqiqa"
                }
                Text("Vaqt: $timeText", color = Slate300, fontSize = 14.sp)
                val formatter = java.text.NumberFormat.getNumberInstance(Locale.US)
                val priceStr = formatter.format(log.price)
                Text("$priceStr so'm", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val (labelColor, labelText, iconColor) = when (log.statusLabel) {
                "COMPLETED" -> Triple(Emerald600.copy(alpha=0.2f), "Tugatildi", Emerald500)
                "FREEPLAY" -> Triple(Indigo600.copy(alpha=0.2f), "Erkin o'yin", Indigo500)
                "CANCELLED" -> Triple(Rose600.copy(alpha=0.2f), "Bekor qilingan", Rose500)
                else -> Triple(StatusEmptyColor, "Tugatildi", Slate400)
            }
            
            Surface(
                color = labelColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(iconColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(labelText, color = iconColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
