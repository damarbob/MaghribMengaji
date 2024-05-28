package com.simsinfotekno.maghribmengaji.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<FirebaseUser>?>()
    val loginResult: LiveData<Result<FirebaseUser>?> get() = _loginResult

    fun loginWithEmailPassword(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login success
                    val user = task.result?.user
                    if (user != null) {
                        _loginResult.value = Result.success(user)
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