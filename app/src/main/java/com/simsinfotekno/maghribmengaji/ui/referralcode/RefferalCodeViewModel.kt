package com.simsinfotekno.maghribmengaji.ui.referralcode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simsinfotekno.maghribmengaji.usecase.UpdateUserProfile
import kotlinx.coroutines.launch

class RefferalCodeViewModel : ViewModel() {


    /* Observables */
    private val _editProfileResult = MutableLiveData<Result<Map<String, Any>>?>(null)
    val editProfileResult: MutableLiveData<Result<Map<String, Any>>?> get() = _editProfileResult


    /* Use cases */
    val updateUserProfile = UpdateUserProfile()

    fun updateProfile(
        userId: String,
        updateData: Map<String, Any>,
    ) {
        viewModelScope.launch {
            updateUserProfile(userId, updateData) { result ->
                result.onSuccess { updatedData ->
                    _editProfileResult.value = Result.success(updatedData)
                }.onFailure { exception ->
                    _editProfileResult.value = Result.failure(exception)
                }
            }
        }
    }
}