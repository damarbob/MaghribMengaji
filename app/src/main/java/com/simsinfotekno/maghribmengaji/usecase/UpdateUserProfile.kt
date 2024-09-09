package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import kotlinx.coroutines.tasks.await

class UpdateUserProfile {

    companion object {
        private const val TAG = "UpdateUserProfile"
    }

     suspend operator fun invoke(
         userId: String,
         updateData: Map<String, Any>,
         onUserProfileUpdated: (Result<Map<String, Any>>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()  // Instantiate Firestore inside the function
        try {
            val collection = db.collection(MaghribMengajiUser.COLLECTION)
            Log.d(TAG, "Updating score for studentId: $userId")

            val querySnapshot = collection
                .whereEqualTo("id", userId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {

                    collection.document(document.id).update(updateData).await()
                    Log.d(TAG, "User profile successfully updated!")
                    onUserProfileUpdated(Result.success(updateData))
                }
            } else {
                Log.w(TAG, "No matching user found")
                onUserProfileUpdated(Result.failure(Exception("No matching user found")))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating user profile", e)
            onUserProfileUpdated(Result.failure(Exception("Error updating user profile. ${e.localizedMessage}")))
        }
    }
}
