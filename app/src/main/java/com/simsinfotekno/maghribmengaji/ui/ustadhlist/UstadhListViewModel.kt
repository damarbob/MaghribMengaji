package com.simsinfotekno.maghribmengaji.ui.ustadhlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class UstadhListViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhListViewModel::class.java.simpleName
    }

    private val _getUstadhResult = MutableLiveData<Result<List<MaghribMengajiUser>>?>()
    val getUstadhResult: LiveData<Result<List<MaghribMengajiUser>>?> get() = _getUstadhResult

    private val _updateUstadhIdResult = MutableLiveData<Result<String>?>()
    val updateUstadhIdResult: LiveData<Result<String>?> get() = _updateUstadhIdResult

    fun updateUserUstadhId(ustadhId: String) {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            db.whereEqualTo("id", currentUser.uid).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {

                            val updateData = mapOf(
                                "ustadhId" to ustadhId, // The new ustadh ID
                                "updatedAt" to FieldValue.serverTimestamp() // Updated at
                            )

                            db.document(document.id).update(updateData)
                                .addOnSuccessListener {
                                    Log.d(MainActivity.TAG, "Ustadh ID successfully updated!")
                                    _updateUstadhIdResult.value = Result.success(ustadhId)

                                    // Update in the repository too
                                    studentRepository.getStudent().ustadhId = ustadhId
                                }
                                .addOnFailureListener { e ->
                                    Log.w(MainActivity.TAG, "Error updating ustadh ID", e)
                                    _updateUstadhIdResult.value = Result.failure(Exception("Error updating ustadh id"))
                                }
                        }
                    } else {
                        Log.w(MainActivity.TAG, "No matching documents found")
                        _updateUstadhIdResult.value = Result.failure(Exception("No matching user found"))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(MainActivity.TAG, "Error getting documents: ", exception)
                    _updateUstadhIdResult.value = Result.failure(Exception("Error updating ustadh id"))
                }
        } else {
            Log.w(MainActivity.TAG, "No current user is logged in")
            _updateUstadhIdResult.value = Result.failure(Exception("No current user is logged in"))
        }
    }


    fun getUstadhFromDb() {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        db.whereEqualTo("role", MaghribMengajiUser.ROLE_TEACHER).get()
            .addOnSuccessListener { documents ->

                val teachers = arrayListOf<MaghribMengajiUser>()

                for (document in documents) {

                    // Get user data
                    val user = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $user")

                    val teacher = MaghribMengajiUser(
                        id = user["id"].toString(),
                        fullName = user["fullName"].toString(),
                        email = user["email"].toString(),
                        //
                    )

                    teachers.add(teacher)

                }
                if (documents.isEmpty) {
                    Log.d(MainActivity.TAG, "No matching documents found.")
                }

                _getUstadhResult.value = Result.success(teachers)

            }
            .addOnFailureListener { exception ->
                Log.w(MainActivity.TAG, "Error getting documents: ", exception)
                _getUstadhResult.value = Result.failure(Exception("Error retrieving ustadh list"))
            }
    }
}