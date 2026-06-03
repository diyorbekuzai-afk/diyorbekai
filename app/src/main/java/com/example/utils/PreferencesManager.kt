package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var notifyTimeUp: Boolean
        get() = prefs.getBoolean("notify_time_up", true)
        set(value) = prefs.edit().putBoolean("notify_time_up", value).apply()

    var notify10Min: Boolean
        get() = prefs.getBoolean("notify_10_min", true)
        set(value) = prefs.edit().putBoolean("notify_10_min", value).apply()

    var notify5Min: Boolean
        get() = prefs.getBoolean("notify_5_min", true)
        set(value) = prefs.edit().putBoolean("notify_5_min", value).apply()

    var notify1Min: Boolean
        get() = prefs.getBoolean("notify_1_min", true)
        set(value) = prefs.edit().putBoolean("notify_1_min", value).apply()

    var ringtoneType: String
        get() = prefs.getString("ringtone_type", "Buzzer") ?: "Buzzer"
        set(value) = prefs.edit().putString("ringtone_type", value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate_enabled", true)
        set(value) = prefs.edit().putBoolean("vibrate_enabled", value).apply()

    var themeMode: Int
        get() = prefs.getInt("theme_mode", 0) // 0 = Auto, 1 = Kunduzgi (Light), 2 = Tungi (Dark)
        set(value) = prefs.edit().putInt("theme_mode", value).apply()

    fun getTableRingtone(tableId: Int): String {
        return prefs.getString("table_ringtone_$tableId", "Global") ?: "Global"
    }

    fun setTableRingtone(tableId: Int, type: String) {
        prefs.edit().putString("table_ringtone_$tableId", type).apply()
    }
}
