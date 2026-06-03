package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(tableId: Int, tableName: String, targetTimeMs: Long) {
        if (targetTimeMs <= System.currentTimeMillis()) return

        // Expired
        scheduleSingleAlarm(tableId, tableName, targetTimeMs, 0)

        // Warnings: 10, 5, 1 minute before
        val tenMin = 10 * 60 * 1000L
        val fiveMin = 5 * 60 * 1000L
        val oneMin = 1 * 60 * 1000L

        if (targetTimeMs - System.currentTimeMillis() > tenMin) {
            scheduleSingleAlarm(tableId, tableName, targetTimeMs - tenMin, 10)
        }
        if (targetTimeMs - System.currentTimeMillis() > fiveMin) {
            scheduleSingleAlarm(tableId, tableName, targetTimeMs - fiveMin, 5)
        }
        if (targetTimeMs - System.currentTimeMillis() > oneMin) {
            scheduleSingleAlarm(tableId, tableName, targetTimeMs - oneMin, 1)
        }
    }

    private fun scheduleSingleAlarm(tableId: Int, tableName: String, targetTimeMs: Long, warningMinutes: Int) {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra(TimerReceiver.EXTRA_TABLE_ID, tableId)
            putExtra(TimerReceiver.EXTRA_TABLE_NAME, tableName)
            putExtra(TimerReceiver.EXTRA_WARNING_MINUTES, warningMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tableId * 100 + warningMinutes,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTimeMs,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTimeMs,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(tableId: Int) {
        cancelSingleAlarm(tableId, 0)
        cancelSingleAlarm(tableId, 10)
        cancelSingleAlarm(tableId, 5)
        cancelSingleAlarm(tableId, 1)
    }

    private fun cancelSingleAlarm(tableId: Int, warningMinutes: Int) {
        val intent = Intent(context, TimerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tableId * 100 + warningMinutes,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
