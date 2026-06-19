package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "cashflow_reminders"
        const val CHANNEL_NAME = "CashFlow Reminders"
        const val DAILY_REMINDER_ID = 1001
        const val EMI_REMINDER_ID = 1002
    }

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        val action = intent?.action
        if (action == "com.example.ACTION_DAILY_REMINDER") {
            // 1. Show daily transactions reminder (at 7 PM)
            showNotification(
                context,
                DAILY_REMINDER_ID,
                "Time for a quick CashFlow Check!",
                "Don't forget to record your today's transactions in CashFlow. Watch your savings grow!"
            )

            // 2. Schedule next day's alarm
            scheduleDailyReminder(context)

            // 3. Check for EMI reminders
            checkAndNotifyEMIs(context)
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use Android standard icon or custom icon if available
        // R.mipmap.ic_launcher is always guaranteed to exist
        val iconRes = R.mipmap.ic_launcher

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Daily bookkeeping and EMI payment reminders"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndNotifyEMIs(context: Context) {
        // Query database on a background coroutine to make sure we don't block the main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                
                // Track for both countries
                val normalEmis = db.emiDao().getAllEMIs(false)
                val secondEmis = db.emiDao().getAllEMIs(true)
                val allEmis = normalEmis + secondEmis
                
                val cal = Calendar.getInstance()
                val currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

                for (emi in allEmis) {
                    val remaining = emi.totalAmount - emi.paidAmount
                    if (remaining > 0.0) {
                        // Extract digit/numeric day from due date (e.g., "10th", "25", "15th")
                        val cleanDayStr = emi.dueDate.filter { it.isDigit() }
                        val dueDay = cleanDayStr.toIntOrNull()
                        if (dueDay != null) {
                            if (dueDay == currentDayOfMonth) {
                                showNotification(
                                    context,
                                    EMI_REMINDER_ID + emi.id,
                                    "EMI Payment Due Today!",
                                    "Your EMI '${emi.title}' of ${emi.totalAmount} is due today. Tap to record the payment."
                                )
                            } else if (dueDay - currentDayOfMonth in 1..2) {
                                // 1-2 days before
                                showNotification(
                                    context,
                                    EMI_REMINDER_ID + emi.id,
                                    "Upcoming EMI Reminder",
                                    "Upcoming payment for '${emi.title}' is due in ${dueDay - currentDayOfMonth} days."
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.ACTION_DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 19) // 7 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if 7pm passed
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to standard set if exact permission is not active
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
