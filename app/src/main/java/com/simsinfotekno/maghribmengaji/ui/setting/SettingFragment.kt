package com.simsinfotekno.maghribmengaji.ui.setting

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.simsinfotekno.maghribmengaji.Notification
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.channelID
import com.simsinfotekno.maghribmengaji.databinding.FragmentSettingBinding
import com.simsinfotekno.maghribmengaji.messageExtra
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.notificationID
import com.simsinfotekno.maghribmengaji.titleExtra
import com.simsinfotekno.maghribmengaji.usecase.CancelDailyNotificationUseCase
import com.simsinfotekno.maghribmengaji.usecase.ScheduleDailyNotificationUseCase
import java.util.Calendar

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private val scheduleDailyNotificationUseCase = ScheduleDailyNotificationUseCase()
    private val cancelDailyNotificationUseCase= CancelDailyNotificationUseCase()

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
        val isMLKitScannerEnabled = MaghribMengajiPref.readBoolean(requireActivity(), MaghribMengajiPref.ML_KIT_SCANNER_ENABLED_KEY, true)
        val calendar = Calendar.getInstance()
        binding.SwitchNotificationButton.isChecked = isNotificationsEnabled
        binding.settingMLKitScannerSwitch.isChecked = isMLKitScannerEnabled

        /* Listeners */
        // Set listener for the Switch
        binding.SwitchNotificationButton.setOnCheckedChangeListener { _, isChecked ->
            MaghribMengajiPref.saveBoolean(requireActivity(), MaghribMengajiPref.NOTIF_ENABLED_KEY, isChecked)

            createNotificationChannel()
            if (isChecked) {
                scheduleDailyNotificationUseCase(requireContext()) // Enable notifications
                Toast.makeText(context, "Alhamdulillah Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()

            } else {
                cancelDailyNotificationUseCase // Disable notifications
                Toast.makeText(context, "Tabarakallah Notifikasi tidak diaktifkan", Toast.LENGTH_SHORT).show()
            }
        }
        binding.settingMLKitScannerSwitch.setOnCheckedChangeListener { _, isChecked ->
            MaghribMengajiPref.saveBoolean(requireActivity(), MaghribMengajiPref.ML_KIT_SCANNER_ENABLED_KEY, isChecked)
        }
        binding.btnTestNotification.setOnClickListener {
            triggerTestNotification()
        }
        binding.settingToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
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
