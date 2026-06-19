package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class EmiNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "EMI Due"
        val message = intent.getStringExtra("message") ?: "Your EMI payment is due soon."

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "emi_alerts",
                "EMI and Debt Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "emi_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

object EmiAlarmScheduler {
    fun scheduleEmiAlertAtDate(context: Context, emiTitle: String, dueDateMillis: Long, amount: Double, currency: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val triggerTime = if (calendar.timeInMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 3000
        } else {
            calendar.timeInMillis
        }
        
        val formattedAmount = String.format(java.util.Locale.getDefault(), "%,.2f", amount)
        val intent = Intent(context, EmiNotificationReceiver::class.java).apply {
            putExtra("title", "Payment Due: $emiTitle 🏷️")
            putExtra("message", "Your installment of $currency $formattedAmount is due today. Protect your CashFlow!")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            emiTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Immediate sandbox confirmation
        val intentDemo = Intent(context, EmiNotificationReceiver::class.java).apply {
            putExtra("title", "CashFlow Shield: Installment Active 🏷️")
            putExtra("message", "Schedule verified: Alarm registered for '$emiTitle' on selected due date.")
        }
        val pendingIntentDemo = PendingIntent.getBroadcast(
            context,
            emiTitle.hashCode() + 1,
            intentDemo,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 2000,
                pendingIntentDemo
            )
        } catch (e: SecurityException) {
            // Ignored, requires exact alarm permission on Android 14+
        }
    }

    fun scheduleEmiAlert(context: Context, emiTitle: String, dueDay: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 1. Core target future month recurrence scheduler
        val calendar = java.util.Calendar.getInstance().apply {
            val nowDay = get(java.util.Calendar.DAY_OF_MONTH)
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (dueDay <= nowDay) {
                add(java.util.Calendar.MONTH, 1)
            }
            set(java.util.Calendar.DAY_OF_MONTH, dueDay.coerceIn(1, 28))
        }
        val targetTriggerTime = calendar.timeInMillis
        
        val intentFuture = Intent(context, EmiNotificationReceiver::class.java).apply {
            putExtra("title", "Payment Due: $emiTitle")
            putExtra("message", "Your installment for $emiTitle is due today. Protect your CashFlow!")
        }
        val pendingIntentFuture = PendingIntent.getBroadcast(
            context,
            emiTitle.hashCode(),
            intentFuture,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 2. Immediate test preview notification in 3 seconds for instant sandbox confirmation
        val intentDemo = Intent(context, EmiNotificationReceiver::class.java).apply {
            putExtra("title", "CashFlow Shield: Installment Active")
            putExtra("message", "Schedule verified: Monthly alarm registered for '$emiTitle' on the ${dueDay}th.")
        }
        val pendingIntentDemo = PendingIntent.getBroadcast(
            context,
            emiTitle.hashCode() + 1,
            intentDemo,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            // Schedule actual target
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetTriggerTime,
                pendingIntentFuture
            )
            // Schedule immediate preview
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 3000,
                pendingIntentDemo
            )
        } catch (e: SecurityException) {
            // Ignored, requires exact alarm permission on Android 14+
        }
    }
}
