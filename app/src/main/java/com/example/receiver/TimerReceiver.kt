package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.utils.AudioGenerator
import com.example.utils.PreferencesManager

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tableId = intent.getIntExtra(EXTRA_TABLE_ID, -1)
        val tableName = intent.getStringExtra(EXTRA_TABLE_NAME) ?: "Stol $tableId"
        val warningMinutes = intent.getIntExtra(EXTRA_WARNING_MINUTES, 0)
        
        val prefs = PreferencesManager(context)
        
        when (warningMinutes) {
            0 -> if (!prefs.notifyTimeUp) return
            10 -> if (!prefs.notify10Min) return
            5 -> if (!prefs.notify5Min) return
            1 -> if (!prefs.notify1Min) return
            else -> return // Ignore unknown warning minutes
        }

        showNotification(context, tableId, tableName, warningMinutes, prefs)
    }

    private fun showNotification(context: Context, tableId: Int, tableName: String, warningMinutes: Int, prefs: PreferencesManager) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "table_timer_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Table Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for table timers"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val i = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            tableId * 100 + warningMinutes,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (warningMinutes == 0) "$tableName vaqti tugadi!" else "$tableName: $warningMinutes minut qoldi!"
        val text = if (warningMinutes == 0) "Stol vaqti tugadi, xizmat ko'rsating." else "Iltimos, tayyorgarlik ko'ring."

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(tableId * 100 + warningMinutes, builder.build())

        // Play Sound
        if (warningMinutes == 0) {
            val tableRingtone = prefs.getTableRingtone(tableId)
            val ringtoneType = if (tableRingtone == "Global") prefs.ringtoneType else tableRingtone

            when {
                ringtoneType == "Beep" -> AudioGenerator.playBeep()
                ringtoneType == "Bell" -> AudioGenerator.playBell()
                ringtoneType == "Buzzer" -> AudioGenerator.playBuzzer()
                ringtoneType == "Whistle" -> AudioGenerator.playWhistle()
                ringtoneType.startsWith("content://") -> {
                    try {
                        val ringtone = android.media.RingtoneManager.getRingtone(context, android.net.Uri.parse(ringtoneType))
                        ringtone?.play()
                        Thread {
                            Thread.sleep(5000)
                            ringtone?.stop()
                        }.start()
                    } catch (e: Exception) {}
                }
                else -> AudioGenerator.playBuzzer()
            }
        } else {
            // Short "Tiyq" for warnings
            AudioGenerator.playBeep()
        }

        // Vibrate
        if (prefs.vibrateEnabled && warningMinutes == 0) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        }
    }

    companion object {
        const val EXTRA_TABLE_ID = "table_id"
        const val EXTRA_TABLE_NAME = "table_name"
        const val EXTRA_WARNING_MINUTES = "warning_minutes"
    }
}
