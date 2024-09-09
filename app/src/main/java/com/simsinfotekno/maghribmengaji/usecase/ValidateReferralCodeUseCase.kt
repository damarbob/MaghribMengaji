package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class ValidateReferralCodeUseCase {
    companion object {
        private val TAG = ValidateReferralCodeUseCase::class.simpleName
    }

    operator fun invoke(referralCode: String, onResult: (Result<MaghribMengajiUser>) -> Unit) {

        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)

        db
            .whereEqualTo("referralCode", referralCode)
            .whereNotEqualTo("role", MaghribMengajiUser.ROLE_STUDENT)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    val data = document.data

                    val user = MaghribMengajiUser(
                        id = data["id"] as String?,
                        fullName = data["fullName"] as String?,
                        email = data["email"] as String?,
                        referralCode = data["referralCode"] as String?,
                        role = data["role"] as String?,
                    )

                    Log.d(TAG, "Referral code found in user ID: ${user.id} => $data")

                    // Call the lambda function with the first user
                    onResult(Result.success(user))
                }

                if (documents.isEmpty) {
                    onResult(Result.failure(Exception("Documents is empty")))
                    Log.d(TAG, "No matching users found.")
                }
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
                Log.e(TAG, "Error getting documents: $e")
            }

    }
}