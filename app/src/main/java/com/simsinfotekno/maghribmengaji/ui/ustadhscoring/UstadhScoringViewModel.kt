package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent

class UstadhScoringViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhScoringFragment::class.java.simpleName
    }

    private val _updateStudentScoreResult = MutableLiveData<Result<String>?>()
    val updateStudentScoreResult: LiveData<Result<String>?> get() = _updateStudentScoreResult

    fun updateStudentScore(studentUserId: String, pageId: Int, tidinessScore: Int, accuracyScore: Int, consistencyScore: Int) {

        val db = Firebase.firestore.collection(QuranPageStudent.COLLECTION)

        Log.d(TAG, "Updating score for studentId: $studentUserId and pageId: $pageId")

        db
            .whereEqualTo("studentId", studentUserId)
            .whereEqualTo("pageId", pageId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {

                        val updateData = mapOf(
                            "tidinessScore" to tidinessScore,
                            "accuracyScore" to accuracyScore,
                            "consistencyScore" to consistencyScore,
                            "updatedAt" to FieldValue.serverTimestamp() // Updated at
                        )

                        db.document(document.id).update(updateData)
                            .addOnSuccessListener {
                                Log.d(MainActivity.TAG, "Student score successfully updated!")
                                _updateStudentScoreResult.value = Result.success(studentUserId)
                            }
                            .addOnFailureListener { e ->
                                Log.w(MainActivity.TAG, "Error updating student score", e)
                                _updateStudentScoreResult.value = Result.failure(Exception("Error updating student score"))
                            }
                    }
                } else {
                    Log.w(MainActivity.TAG, "No matching student found")
                    _updateStudentScoreResult.value = Result.failure(Exception("No matching student found"))
                }
            }
            .addOnFailureListener { exception ->
                Log.w(MainActivity.TAG, "Error getting documents: ", exception)
                _updateStudentScoreResult.value = Result.failure(Exception("Error updating student score. ${exception.localizedMessage}"))
            }
    }

}