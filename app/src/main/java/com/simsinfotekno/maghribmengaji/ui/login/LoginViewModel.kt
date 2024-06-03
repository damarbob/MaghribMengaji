package com.simsinfotekno.maghribmengaji.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class LoginViewModel : ViewModel() {

    companion object {
        private val TAG = LoginViewModel::class.java.simpleName
    }

    private val _loginResult = MutableLiveData<Result<FirebaseUser>?>()
    val loginResult: LiveData<Result<FirebaseUser>?> get() = _loginResult

    fun loginWithEmailPassword(email: String, password: String) {

        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)

        // Sign in with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Login success
                    val user = task.result?.user
                    if (user != null) {

                        db.whereEqualTo("id", user.uid).get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    Log.d(TAG, "Document found with ID: ${document.id} => ${document.data}")

                                    _loginResult.value = Result.success(user)
                                }
                                if (documents.isEmpty) {
                                    Log.d(TAG, "No matching documents found.")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting documents: ", exception)
                            }

                        val student = MaghribMengajiUser(
                            id = user.uid,
                            fullName = user.displayName,
                            email = user.email,
                            //
                        )
                        studentRepository.setStudent(student) // Set newStudent to repository
                    } else {
                        _loginResult.value = Result.failure(Exception("User is null"))
                    }
                } else {
                    // If login fails, display a message to the user.
                    _loginResult.value = Result.failure(task.exception ?: Exception("Login failed"))
                }
            }
    }
}