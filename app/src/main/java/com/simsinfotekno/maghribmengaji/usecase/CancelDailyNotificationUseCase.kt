package com.simsinfotekno.maghribmengaji.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity.ALARM_SERVICE
import com.simsinfotekno.maghribmengaji.Notification
import com.simsinfotekno.maghribmengaji.notificationID
//cancel penjadwalan notifikasi
class CancelDailyNotificationUseCase {
    operator fun invoke(context: Context){
        val intent = Intent(context.applicationContext, Notification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            notificationID,
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}