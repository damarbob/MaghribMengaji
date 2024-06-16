package com.simsinfotekno.maghribmengaji.ui.ustadhhome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhStudentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class UstadhHomeViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhHomeViewModel::class.java.simpleName
    }

    private val _getStudentResult = MutableLiveData<Result<List<MaghribMengajiUser>>?>()
    val getStudentResult: LiveData<Result<List<MaghribMengajiUser>>?> get() = _getStudentResult

    fun getStudentFromDb(ustadhUserId: String) {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        db.whereEqualTo("role", MaghribMengajiUser.ROLE_STUDENT)
            .whereEqualTo("ustadhId", ustadhUserId)
            .get()
            .addOnSuccessListener { documents ->

                val students = arrayListOf<MaghribMengajiUser>()

                for (document in documents) {

                    // Get user data
                    val user = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $user")

                    val teacher = MaghribMengajiUser(
                        id = user["id"].toString(),
                        fullName = user["fullName"].toString(),
                        email = user["email"].toString(),
                        // TODO: Add phone number for all user model including in the sign up
                    )

                    students.add(teacher)

                }
                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }

                _getStudentResult.value = Result.success(students)

                // Insert into repository
                ustadhStudentRepository.setRecords(students, false)

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                _getStudentResult.value = Result.failure(Exception("Error retrieving ustadh list"))
            }
    }

}