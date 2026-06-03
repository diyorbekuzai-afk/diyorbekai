package com.example.ui.screens

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TableInfo
import com.example.ui.theme.*
import com.example.utils.AudioGenerator
import com.example.utils.PreferencesManager
import com.example.viewmodel.TableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TableViewModel,
    onThemeModeChanged: ((Int) -> Unit)? = null
) {
    val tables by viewModel.tables.collectAsState()
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    
    var notifyTimeUp by remember { mutableStateOf(prefs.notifyTimeUp) }
    var notify10Min by remember { mutableStateOf(prefs.notify10Min) }
    var notify5Min by remember { mutableStateOf(prefs.notify5Min) }
    var notify1Min by remember { mutableStateOf(prefs.notify1Min) }
    var ringtoneType by remember { mutableStateOf(prefs.ringtoneType) }
    var vibrateEnabled by remember { mutableStateOf(prefs.vibrateEnabled) }
    var themeMode by remember { mutableIntStateOf(prefs.themeMode) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sozlamalar", color = MainTextColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                // Theme Settings
                item {
                    Text("Tashqi ko'rinish", color = Slate300, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            val themeOptions = listOf("Avtomat (Tizim / Vaqt)", "Kunduzgi rejim (Yorug')", "Tungi rejim (Qorong'u)")
                            themeOptions.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            themeMode = index
                                            prefs.themeMode = index
                                            onThemeModeChanged?.invoke(index)
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option, color = MainTextColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                    RadioButton(
                                        selected = themeMode == index,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald500, unselectedColor = Slate400)
                                    )
                                }
                                if (index < themeOptions.size - 1) {
                                    HorizontalDivider(color = StatusEmptyColor)
                                }
                            }
                        }
                    }
                }

                // Settings components
                item {
                    Text("Ogohlantirish va Bildirishnomalar", color = Slate300, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            SettingsSwitchItem(
                                title = "Vaqt tugaganda bildirish",
                                checked = notifyTimeUp,
                                onCheckedChange = {
                                    notifyTimeUp = it
                                    prefs.notifyTimeUp = it
                                }
                            )
                            HorizontalDivider(color = StatusEmptyColor)
                            SettingsSwitchItem(
                                title = "10 daqiqa qolganda bildirish",
                                checked = notify10Min,
                                onCheckedChange = {
                                    notify10Min = it
                                    prefs.notify10Min = it
                                }
                            )
                            HorizontalDivider(color = StatusEmptyColor)
                            SettingsSwitchItem(
                                title = "5 daqiqa qolganda bildirish",
                                checked = notify5Min,
                                onCheckedChange = {
                                    notify5Min = it
                                    prefs.notify5Min = it
                                }
                            )
                            HorizontalDivider(color = StatusEmptyColor)
                            SettingsSwitchItem(
                                title = "1 daqiqa qolganda bildirish",
                                checked = notify1Min,
                                onCheckedChange = {
                                    notify1Min = it
                                    prefs.notify1Min = it
                                }
                            )
                            HorizontalDivider(color = StatusEmptyColor)
                            SettingsSwitchItem(
                                title = "Vibratsiya",
                                checked = vibrateEnabled,
                                onCheckedChange = {
                                    vibrateEnabled = it
                                    prefs.vibrateEnabled = it
                                }
                            )
                        }
                    }
                }

                item {
                    Text("Signal Ovozi", color = Slate300, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            val options = listOf("Beep", "Bell", "Buzzer", "Whistle")
                            
                            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                                androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                if (result.resultCode == android.app.Activity.RESULT_OK) {
                                    val uri: android.net.Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI, android.net.Uri::class.java)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                                    }
                                    if (uri != null) {
                                        val uriStr = uri.toString()
                                        ringtoneType = uriStr
                                        prefs.ringtoneType = uriStr
                                        try {
                                            val ringtone = android.media.RingtoneManager.getRingtone(context, android.net.Uri.parse(uriStr))
                                            ringtone?.play()
                                            java.lang.Thread {
                                                java.lang.Thread.sleep(3000)
                                                ringtone?.stop()
                                            }.start()
                                        } catch(e: Exception) {}
                                    }
                                }
                            }
                            
                            options.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ringtoneType = option
                                            prefs.ringtoneType = option
                                            when (option) {
                                                "Beep" -> AudioGenerator.playBeep()
                                                "Bell" -> AudioGenerator.playBell()
                                                "Buzzer" -> AudioGenerator.playBuzzer()
                                                "Whistle" -> AudioGenerator.playWhistle()
                                            }
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = if (ringtoneType == option) Emerald500 else Slate400)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(option, color = MainTextColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                    RadioButton(
                                        selected = (ringtoneType == option),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald500, unselectedColor = Slate400)
                                    )
                                }
                                HorizontalDivider(color = StatusEmptyColor)
                            }

                            // Custom Device Ringtone option
                            val isCustom = ringtoneType.startsWith("content://")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER)
                                        intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION or android.media.RingtoneManager.TYPE_ALARM)
                                        intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                                        intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                        launcher.launch(intent)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = if (isCustom) Emerald500 else Slate400)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(if (isCustom) "Maxsus (telefondan)" else "Qurilmadan tanlash...", color = if (isCustom) MainTextColor else Indigo500, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                RadioButton(
                                    selected = isCustom,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Emerald500, unselectedColor = Slate400)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stol nomlari va vaqtlari", color = Slate300, fontSize = 14.sp)
                }
                
                items(tables, key = { it.id }) { table ->
                    TableSettingItem(table, viewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MainTextColor, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Emerald500)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSettingItem(table: TableInfo, viewModel: TableViewModel) {
    var timeInput by remember { mutableStateOf((table.defaultDurationMs / 60000).toString()) }
    var nameInput by remember { mutableStateOf(table.name) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.example.utils.PreferencesManager(context) }
    var currentRingtone by remember { mutableStateOf(prefs.getTableRingtone(table.id)) }
    var showRingtoneMenu by remember { mutableStateOf(false) }

    val playSound: (String) -> Unit = { type ->
        when (type) {
            "Beep" -> com.example.utils.AudioGenerator.playBeep()
            "Bell" -> com.example.utils.AudioGenerator.playBell()
            "Buzzer" -> com.example.utils.AudioGenerator.playBuzzer()
            "Whistle" -> com.example.utils.AudioGenerator.playWhistle()
            else -> {
                if (type.startsWith("content://")) {
                     try {
                         val ringtone = android.media.RingtoneManager.getRingtone(context, android.net.Uri.parse(type))
                         ringtone?.play()
                         java.lang.Thread {
                             java.lang.Thread.sleep(3000)
                             ringtone?.stop()
                         }.start()
                     } catch(e: Exception) {}
                }
            }
        }
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: android.net.Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                val uriStr = uri.toString()
                currentRingtone = uriStr
                prefs.setTableRingtone(table.id, uriStr)
                playSound(uriStr)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Stol nomi", color = Slate400) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MainTextColor,
                    unfocusedTextColor = Slate300,
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate600
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Standart vaqt (minut)", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MainTextColor,
                        unfocusedTextColor = Slate300,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate600
                    )
                )
                
                Button(
                    onClick = {
                        val minutes = timeInput.toLongOrNull()
                        if (minutes != null && minutes > 0) {
                            // using addTimeToTable for empty tables changes defaultDurationMs
                            viewModel.addTimeToTable(table.id, minutes * 60000L)
                            viewModel.changeTableName(table.id, nameInput)
                        }
                    },
                    modifier = Modifier.height(56.dp).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Saqlash", color = MainTextColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ringtoneName = when {
                    currentRingtone == "Global" -> "Sozlamalardagi asosiy ovoz"
                    currentRingtone.startsWith("content://") -> "Fayldan (Maxsus)"
                    else -> currentRingtone
                }

                Text("Signal: ", color = Slate400, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                
                Box {
                    Button(
                        onClick = { showRingtoneMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmptyColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(ringtoneName, color = Emerald500, fontSize = 13.sp)
                    }

                    DropdownMenu(
                        expanded = showRingtoneMenu,
                        onDismissRequest = { showRingtoneMenu = false },
                        modifier = Modifier.background(CardColor)
                    ) {
                        val options = listOf("Global", "Beep", "Bell", "Buzzer", "Whistle")
                        options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(if (opt == "Global") "Asosiy (Standart)" else opt, color = MainTextColor) },
                                onClick = {
                                    currentRingtone = opt
                                    prefs.setTableRingtone(table.id, opt)
                                    showRingtoneMenu = false
                                    if (opt != "Global") playSound(opt)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Qurilmadan tanlash...", color = Indigo500) },
                            onClick = {
                                showRingtoneMenu = false
                                val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION or android.media.RingtoneManager.TYPE_ALARM)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                launcher.launch(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}
