package com.simsinfotekno.maghribmengaji.usecase

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.simsinfotekno.maghribmengaji.Notification
import com.simsinfotekno.maghribmengaji.messageExtra
import com.simsinfotekno.maghribmengaji.notificationID
import com.simsinfotekno.maghribmengaji.titleExtra
import java.util.Calendar

class ScheduleDailyNotificationUseCase {
    @SuppressLint("ScheduleExactAlarm")
    operator fun invoke(context: Context) {

        val intent = Intent(context.applicationContext, Notification::class.java)
        // Title and message can be hardcoded or dynamically set
        val title = "Maghrib Mengaji"
        val message = "Jangan lupa untuk mengaji"

        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            notificationID,
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule the notification at 6 PM every day
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the time has already passed for today, set it for tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY, // Repeat every day
            //10 * 60 * 1000L
            pendingIntent
        )


    }
}