package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TableRepository
import com.example.receiver.AlarmScheduler
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TableViewModel
import com.example.viewmodel.TableViewModelFactory
import com.example.ui.theme.CardColor
import com.example.ui.theme.MainTextColor
import com.example.ui.theme.Slate400
import com.example.ui.theme.Emerald500

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "table_tennis_db"
        ).fallbackToDestructiveMigration().build()
        
        val repository = TableRepository(db.tableDao())
        val alarmScheduler = AlarmScheduler(this)
        
        val factory = TableViewModelFactory(repository, alarmScheduler)
        viewModel = ViewModelProvider(this, factory)[TableViewModel::class.java]

        setContent {
            var showPermissionDialog by remember { mutableStateOf(false) }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                showPermissionDialog = false
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        showPermissionDialog = true
                    }
                }
            }

            val prefs = remember { com.example.utils.PreferencesManager(this@MainActivity) }
            var themeMode by remember { mutableIntStateOf(prefs.themeMode) }

            val darkTheme = when(themeMode) {
                1 -> false
                2 -> true
                else -> com.example.ui.theme.isNightTime()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    Scaffold(
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route ?: "main"

                            Surface(
                                modifier = Modifier.fillMaxWidth().shadow(10.dp),
                                color = CardColor
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                            navController.navigate("main") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                        IconButton(onClick = {
                                            navController.navigate("main") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                            Icon(Icons.Default.Home, contentDescription = null, tint = if (currentRoute == "main") Emerald500 else Slate400)
                                        }
                                        Text("Bosh sahifa", color = if (currentRoute == "main") Emerald500 else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                            navController.navigate("history") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                        IconButton(onClick = {
                                            navController.navigate("history") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = if (currentRoute == "history") Emerald500 else Slate400)
                                        }
                                        Text("Tarix", color = if (currentRoute == "history") Emerald500 else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                            navController.navigate("settings") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                        IconButton(onClick = {
                                            navController.navigate("settings") {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = if (currentRoute == "settings") Emerald500 else Slate400)
                                        }
                                        Text("Sozlamalar", color = if (currentRoute == "settings") Emerald500 else Slate400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(navController = navController, startDestination = "main", modifier = Modifier.padding(innerPadding)) {
                            composable("main") {
                                MainScreen(viewModel = viewModel)
                            }
                            composable("history") {
                                HistoryScreen(viewModel = viewModel)
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onThemeModeChanged = { themeMode = it }
                                )
                            }
                        }
                    }
                    
                    if (showPermissionDialog) {
                        Dialog(onDismissRequest = {}) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardColor,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tizimni faollashtirish",
                                        color = MainTextColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Ovozli signal va xabarnomalar to'g'ri ishlashi uchun ruxsat bering.",
                                        color = Slate400,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                showPermissionDialog = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("Ruxsat berish", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

