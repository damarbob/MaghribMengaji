package com.simsinfotekno.maghribmengaji.ui.ustadhlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class UstadhListViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhListViewModel::class.java.simpleName
    }

    private val _getUstadhResult = MutableLiveData<Result<List<MaghribMengajiUser>>?>()
    val getUstadhResult: LiveData<Result<List<MaghribMengajiUser>>?> get() = _getUstadhResult

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