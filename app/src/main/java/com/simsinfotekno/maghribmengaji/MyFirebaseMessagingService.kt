package com.simsinfotekno.maghribmengaji

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")

        val userId = Firebase.auth.currentUser?.uid  // Get the currently logged-in user's ID
        if (userId != null) {
            saveTokenToFirestore(userId, token)
            retryPendingNotifications(userId)
        }
    }

    private fun saveTokenToFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("Firestore", "Token updated.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update token", e)
            }
    }

    private fun retryPendingNotifications(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pendingNotifications")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val notification = doc.get("message") as Map<String, String>
                    sendNotification(notification)
                    doc.reference.delete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch pending notifications", e)
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle foreground notifications
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(notification: Map<String, String>) {
        val builder = NotificationCompat.Builder(this, "default_channel")
            .setContentTitle(notification["title"])
            .setContentText(notification["body"])
            .setSmallIcon(R.drawable.ic_launcher_maghrib_mengaji_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

//        with(NotificationManagerCompat.from(this)) {
//            notify(System.currentTimeMillis().toInt(), builder.build())
//        }
        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8.0+ (Oreo), create a notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, notificationBuilder.build())
    }
}