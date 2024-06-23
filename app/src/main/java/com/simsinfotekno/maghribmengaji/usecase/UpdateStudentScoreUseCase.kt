package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import kotlinx.coroutines.tasks.await

class UpdateStudentScoreUseCase {

    companion object {
        private const val TAG = "UpdateStudentScoreUseCase"
    }

     suspend operator fun invoke(
        studentUserId: String,
        pageId: Int,
        tidinessScore: Int,
        accuracyScore: Int,
        consistencyScore: Int,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()  // Instantiate Firestore inside the function
        try {
            val collection = db.collection(QuranPageStudent.COLLECTION)
            Log.d(TAG, "Updating score for studentId: $studentUserId and pageId: $pageId")

            val querySnapshot = collection
                .whereEqualTo("studentId", studentUserId)
                .whereEqualTo("pageId", pageId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    val updateData = mapOf(
                        "tidinessScore" to tidinessScore,
                        "accuracyScore" to accuracyScore,
                        "consistencyScore" to consistencyScore,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    collection.document(document.id).update(updateData).await()
                    Log.d(TAG, "Student score successfully updated!")
                    onSuccess(studentUserId)
                }
            } else {
                Log.w(TAG, "No matching student found")
                onFailure(Exception("No matching student found"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating student score", e)
            onFailure(Exception("Error updating student score. ${e.localizedMessage}"))
        }
    }
}
