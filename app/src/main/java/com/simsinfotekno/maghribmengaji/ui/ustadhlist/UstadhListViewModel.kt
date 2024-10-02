package com.simsinfotekno.maghribmengaji.ui.ustadhlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.get
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
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
                                    studentRepository.getStudent()?.ustadhId = ustadhId
                                }
                                .addOnFailureListener { e ->
                                    Log.w(MainActivity.TAG, "Error updating ustadh ID", e)
                                    _updateUstadhIdResult.value =
                                        Result.failure(Exception("Error updating ustadh id"))
                                }
                        }
                    } else {
                        Log.w(MainActivity.TAG, "No matching documents found")
                        _updateUstadhIdResult.value =
                            Result.failure(Exception("No matching user found"))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(MainActivity.TAG, "Error getting documents: ", exception)
                    _updateUstadhIdResult.value =
                        Result.failure(Exception("Error updating ustadh id"))
                }
        } else {
            Log.w(MainActivity.TAG, "No current user is logged in")
            _updateUstadhIdResult.value = Result.failure(Exception("No current user is logged in"))
        }
    }

    fun getUstadhFromDb() {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        val query = db.whereEqualTo("role", MaghribMengajiUser.ROLE_TEACHER)
        val currentUserSchool = studentRepository.getStudent()?.school

        if (currentUserSchool != null) {
            query.whereEqualTo("school", currentUserSchool).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // No teachers found for the specific school, fallback to all teachers
                        getAllTeachers()
                    } else {
                        // Process the documents as before
                        processTeacherDocuments(documents)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                    _getUstadhResult.value =
                        Result.failure(Exception("Error retrieving ustadh list"))
                }
        } else {
            //If currentUserSchool is null, directly get all teachers
            getAllTeachers()
        }
    }

    private fun getAllTeachers() {
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        db.whereEqualTo("role", MaghribMengajiUser.ROLE_TEACHER).get()
            .addOnSuccessListener { documents ->
                processTeacherDocuments(documents)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                _getUstadhResult.value = Result.failure(Exception("Error retrieving ustadh list"))
            }
    }

    private fun processTeacherDocuments(documents: QuerySnapshot) {
        val teachers = arrayListOf<MaghribMengajiUser>()
        for (document in documents) {
            val user = document.data
            Log.d(TAG, "Document found with ID: ${document.id} => $user")
            val teacher = MaghribMengajiUser(
                id = user["id"].toString(),
                fullName = user["fullName"].toString(),
                email = user["email"].toString(),
                // ... other fields
            )
            teachers.add(teacher)
        }

        if (documents.isEmpty) {
            Log.d(TAG, "No matching documents found.")
        }

        _getUstadhResult.value = Result.success(teachers)
    }
//    fun sendNotificationToUstadh(ustadhId: String) {
//        // Call to backend API to send a notification
//        val notificationRequest = NotificationRequest(ustadhId, "New Student", "A student has chosen you as their ustadh.")
//        apiService.sendNotification(notificationRequest).enqueue(object : Callback<Unit> {
//            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                Log.d("Notification", "Notification sent to ustadh successfully")
//            }
//
//            override fun onFailure(call: Call<Unit>, t: Throwable) {
//                Log.e("Notification", "Failed to send notification", t)
//            }
//        })
//    }



//    fun getUstadhFromDb() {
//        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
//        db.whereEqualTo("role", MaghribMengajiUser.ROLE_TEACHER).get()
//            .addOnSuccessListener { documents ->
//
//                val teachers = arrayListOf<MaghribMengajiUser>()
//
//                for (document in documents) {
//
//                    // Get user data
//                    val user = document.data
//
//                    Log.d(TAG, "Document found with ID: ${document.id} => $user")
//
//                    val teacher = MaghribMengajiUser(
//                        id = user["id"].toString(),
//                        fullName = user["fullName"].toString(),
//                        email = user["email"].toString(),
//                        //
//                    )
//
//                    teachers.add(teacher)
//
//                }
//                if (documents.isEmpty) {
//                    Log.d(MainActivity.TAG, "No matching documents found.")
//                }
//
//                _getUstadhResult.value = Result.success(teachers)
//
//            }
//            .addOnFailureListener { exception ->
//                Log.w(MainActivity.TAG, "Error getting documents: ", exception)
//                _getUstadhResult.value =
//                    Result.failure(Exception("Error retrieving ustadh list"))
//            }
//    }
}
