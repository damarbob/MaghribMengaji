package com.simsinfotekno.maghribmengaji.ui.setting

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.app.AppCompatActivity.ALARM_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.simsinfotekno.maghribmengaji.Notification
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.channelID
import com.simsinfotekno.maghribmengaji.databinding.FragmentSettingBinding
import com.simsinfotekno.maghribmengaji.messageExtra
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.notificationID
import com.simsinfotekno.maghribmengaji.titleExtra
import com.simsinfotekno.maghribmengaji.usecase.ScheduleDailyNotificationUseCase
import java.util.Calendar

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private val scheduleDailyNotificationUseCase = ScheduleDailyNotificationUseCase()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(layoutInflater,container,false)

        // Initialize the Switch with saved preference
        val sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
        val isNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)
        val calendar = Calendar.getInstance()
        binding.SwitchNotificationButton.isChecked = isNotificationsEnabled

//        createNotificationChannel()
//        scheduleDailyNotification()

        // Set listener for the Switch
        binding.SwitchNotificationButton.setOnCheckedChangeListener { _, isChecked ->
            MaghribMengajiPref.saveBoolean(requireActivity(), MaghribMengajiPref.NOTIF_ENABLED_KEY, isChecked)

            createNotificationChannel()
            if (isChecked) {
                scheduleDailyNotificationUseCase(requireContext()) // Enable notifications
            } else {
                cancelDailyNotification() // Disable notifications
            }
        }
        binding.btnTestNotification.setOnClickListener {
            triggerTestNotification()

        }
        /* Listeners */
        binding.volumeListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()

        }
        return binding.root
    }



    private fun cancelDailyNotification() {
        val intent = Intent(requireContext().applicationContext, Notification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    //untuk android diatas 8 butuh notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Notification Channel"
            val desc = "Channel for daily notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = desc
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } else {
            // No need to create a notification channel on Android 7 or lower
            // Just show the notification directly
        }
    }
    private fun triggerTestNotification() {
        val intent = Intent(requireContext().applicationContext, Notification::class.java)

        val title = "Test Notification"
        val message = "This is a test notification"

        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        pendingIntent.send() // Trigger the notification immediately
    }
}
