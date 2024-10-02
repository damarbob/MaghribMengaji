package com.simsinfotekno.maghribmengaji.usecase

import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.simsinfotekno.maghribmengaji.R

class RetriveFCMkeyFromUser {

    fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        // Get the FCM token
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
//                return@addOnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            // Log and use the token as needed
//            Log.d("FCM", "FCM Token: $token")
//        }

        // Get the FCM token and store it in Firestore for the logged-in teacher
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    // Get Firestore instance
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)

                    // Check if the user has the role of "teacher"
                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role")
                            if (role == "teacher") {
                                // Save the FCM token if the user is a teacher
                                userRef.update("fcmToken", fcmToken)
                                    .addOnSuccessListener {
                                        Log.d("FCM", "Token successfully updated for teacher")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("FCM", "Error updating token", e)
                                    }
                            } else {
                                Log.d("FCM", "User is not a teacher, FCM token not stored")
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.w("FCM", "Error fetching user data", e)
                    }
                }
            } else {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            }
        }
    }


    private fun setContentView(activityMain: Int) {

    }
}