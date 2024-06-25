package com.simsinfotekno.maghribmengaji

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

interface ActivityRestartable {
    fun restartActivity() {

        val activity = this as AppCompatActivity
        val intent = Intent(activity, activity::class.java)

        activity.startActivity(intent)
        activity.finish()
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

    }

    fun restartApplication() {

        val activity = this as AppCompatActivity

        val intent = Intent(activity.applicationContext, MainActivity::class.java)  // Replace with your launcher activity
        val pendingIntentId = 123456
        val pendingIntent = PendingIntent.getActivity(
            activity.applicationContext,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        activity.finishAffinity()
    }
}