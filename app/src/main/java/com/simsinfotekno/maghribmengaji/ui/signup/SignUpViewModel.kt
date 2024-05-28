package com.simsinfotekno.maghribmengaji.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpViewModel : ViewModel() {
    private val _authResult = MutableLiveData<Result<FirebaseUser>?>()
    val authResult: LiveData<Result<FirebaseUser>?> get() = _authResult

    fun signUpWithEmailPassword(displayName: String, email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    val user = task.result?.user
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    _authResult.value = Result.success(user)
                                } else {
                                    _authResult.value = Result.failure(profileUpdateTask.exception ?: Exception("Failed to update profile"))
                                }
                            }
                    } else {
                        _authResult.value = Result.failure(Exception("User is null"))
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    _authResult.value = Result.failure(task.exception ?: Exception("Sign up failed"))
                }
            }
    }
}