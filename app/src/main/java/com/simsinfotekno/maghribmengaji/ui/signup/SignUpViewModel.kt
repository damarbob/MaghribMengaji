package com.simsinfotekno.maghribmengaji.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser


class SignUpViewModel : ViewModel() {
    private val _authResult = MutableLiveData<Result<FirebaseUser>?>()
    val authResult: LiveData<Result<FirebaseUser>?> get() = _authResult

    fun signUpWithEmailPassword(displayName: String, email: String, phone: String, password: String) {

        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        val auth = FirebaseAuth.getInstance()

        // Create a new user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Sign up success
                    val user = task.result?.user
                    if (user != null) {

                        // Update display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {

                                    // TODO: Move to use case
                                    // Create new student instance
                                    val newStudent = MaghribMengajiUser(
                                        id = user.uid,
                                        fullName = displayName,
                                        email = user.email,
                                        phone = phone,
                                        lastPageId = null,
                                        ustadhId = null,
                                        createdAt = Timestamp.now(),
                                        updatedAt = Timestamp.now(),
                                    )

                                    db.add(newStudent).addOnCompleteListener{
                                        if (it.isSuccessful) {

                                            studentRepository.setStudent(newStudent) // Set newStudent to repository
                                            _authResult.value = Result.success(user) // Return user

                                        }
                                        else {
                                            _authResult.value = Result.failure(it.exception ?: Exception("Failed to create newStudent data"))
                                        }
                                    }

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