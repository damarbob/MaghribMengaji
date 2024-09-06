package com.simsinfotekno.maghribmengaji.ui.editprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.simsinfotekno.maghribmengaji.usecase.UpdateUserProfile
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    /* Observables */
    private val _editProfileResult = MutableLiveData<Result<String>?>(null)
    val editProfileResult: MutableLiveData<Result<String>?> get() = _editProfileResult
    private val _authUpdateProfileResult = MutableLiveData<Result<String>?>(null)
    val authUpdateProfileResult: MutableLiveData<Result<String>?> get() = _authUpdateProfileResult

    /* Use cases */
    private val updateUserProfile = UpdateUserProfile()

    fun resetLiveData()
    {
        _editProfileResult.value = null
    }

    fun updateProfile(
        userId: String,
        updateData: Map<String, Any>,
    ) {
        viewModelScope.launch {
            updateUserProfile(
                userId,
                updateData,
                onSuccess = { studentId ->

                    // Update display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(updateData["fullName"].toString())
                        .build()
                    Firebase.auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileUpdateTask ->
                            if (profileUpdateTask.isSuccessful) {
                                _authUpdateProfileResult.value = Result.success(studentId)
                            } else {
                                _authUpdateProfileResult.value = profileUpdateTask.exception?.let {
                                    Result.failure(
                                        it
                                    )
                                }
                            }
                        }

                    _editProfileResult.value = Result.success(studentId)
                },
                onFailure = { exception ->
                    _editProfileResult.value = Result.failure(exception)
                }
            )
        }
    }

}