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
         onSuccess: (String) -> Unit,
         onFailure: (Exception) -> Unit
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
                    onSuccess(userId)
                }
            } else {
                Log.w(TAG, "No matching user found")
                onFailure(Exception("No matching user found"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating user profile", e)
            onFailure(Exception("Error updating user profile. ${e.localizedMessage}"))
        }
    }
}
