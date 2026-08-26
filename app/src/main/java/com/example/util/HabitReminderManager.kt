package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Calendar

object HabitReminderManager {

    private const val CHANNEL_ID = "daily_habit_reminders_channel"
    private const val CHANNEL_NAME = "Daily Habit Reminders"
    private const val NOTIFICATION_ID = 8001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily 8:00 PM reminder to log pending habits and maintain streaks."
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun isPast8Pm(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour >= 20
    }

    fun shouldShow8PmReminder(
        totalHabitsCount: Int,
        completedTodayCount: Int,
        forceCheck: Boolean = false
    ): Boolean {
        if (totalHabitsCount == 0) return false
        val timeCondition = isPast8Pm() || forceCheck
        return timeCondition && completedTodayCount == 0
    }

    fun postSystemNotification(context: Context, totalHabits: Int) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔥 8:00 PM Habit Check-in")
            .setContentText("You haven't marked your $totalHabits habits today! Log now to keep your streaks.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's 8:00 PM and none of your $totalHabits daily habits are marked complete yet. Check in now to maintain your consistency and streak score!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            // Permission or security exception
        }
    }
}
