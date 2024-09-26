package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
                        phone = data["phone"] as String?,
                        address = data["address"] as String?,
                        school = data["school"] as String?,
                        referralCode = data["referralCode"] as String?,
                        balance = (data["balance"] as Long?)?.toInt(),
                        bank = data["bank"] as String?,
                        bankAccount = data["bankAccount"] as String?,
                        ownedVolumeId = data["ownedVolumeId"] as? List<Int>?,
                        role = data["role"] as String?,
                        lastPageId = (data["lastPageId"] as Long?)?.toInt(),
                        ustadhId = data["ustadhId"] as String?,
                        lastDailySubmit = data["lastDailySubmit"] as Timestamp?,
                        currentSubmitStreak = (data["currentSubmitStreak"] as Long?)?.toInt(),
                    )

                    // Call the lambda function with the retrieved student
                    onUserProfileRetrieved(student)
                }

                // If user's document is nowhere to be found, create new
                if (documents.isEmpty) {

                    val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
                    val auth = FirebaseAuth.getInstance()

                    Log.d(TAG, "No matching documents found. Creating new document for ${auth.currentUser?.email}")

                    val newStudent = MaghribMengajiUser(
                        id = auth.currentUser?.uid,
                        fullName = auth.currentUser?.displayName,
                        email = auth.currentUser?.email,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                    )

                    db.add(newStudent).addOnCompleteListener{
                        if (it.isSuccessful) {

                            onUserProfileRetrieved(newStudent)

                        }
                        else {
                            Log.w(TAG, "Error getting documents: ", it.exception)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

}