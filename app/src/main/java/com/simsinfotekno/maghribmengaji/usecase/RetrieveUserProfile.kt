package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class RetrieveUserProfile {

    companion object {
        private val TAG = RetrieveUserProfile::class.simpleName
    }

    // Method to retrieve the user profile
    operator fun invoke(userId: String, onUserProfileRetrieved: (MaghribMengajiUser) -> Unit) {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)

        db.whereEqualTo("id", userId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    // Get user data
                    val data = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $data")

                    val student = MaghribMengajiUser(
                        userId,
                        fullName = data["fullName"] as String?,
                        email = data["email"] as String?,
                        role = data["role"] as String?,
                        lastPageId = data["lastPageId"] as Int?,
                        ustadhId = data["ustadhId"] as String?,
                    )

                    // Call the lambda function with the retrieved student
                    onUserProfileRetrieved(student)
                }

                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

}