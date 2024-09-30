package com.simsinfotekno.maghribmengaji.ui.signup

import android.util.Log
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
import com.simsinfotekno.maghribmengaji.usecase.TransactionService
import com.simsinfotekno.maghribmengaji.usecase.ValidateReferralCodeUseCase


class SignUpViewModel : ViewModel() {
    private val _authResult = MutableLiveData<Result<FirebaseUser>?>()
    val authResult: LiveData<Result<FirebaseUser>?> get() = _authResult

    private val _progressVisibility = MutableLiveData<Boolean>(false)
    val progressVisibility: LiveData<Boolean> get() = _progressVisibility

    /* Use cases */
    private val validateReferralCodeUseCase = ValidateReferralCodeUseCase()
    private val transactionService = TransactionService()

    private var referredUser: MaghribMengajiUser? = null

    fun signUpWithEmailPassword(
        displayName: String,
        email: String,
        phone: String,
        password: String,
        address: String,
        school: String,
        referral: String
    ) {

        _progressVisibility.value = true

        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        val auth = FirebaseAuth.getInstance()
        var isReferralValid = false

        // Validate referral code
        validateReferralCodeUseCase(referral) {
            it.onSuccess { user ->

                isReferralValid = true // Referral code is VALID
                referredUser = user
                Log.d("SignUpViewModel", "Referral code is VALID")
            }.onFailure { e ->
                // If referral check fails, display a message to the user.
//                _authResult.value = Result.failure(e)
//                _progressVisibility.value = false
                Log.d("SignUpViewModel", "Referral code is INVALID")
            }
        }

        // Create a new user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SignUpViewModel", "Sign up successful")
                    Log.d("SignUpViewModel", "User: ${task.result?.user}")

                    // Sign up success
                    val user = task.result?.user
                    if (user != null) {

                        // Update display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()

                        // Update user data
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    Log.d("SignUpViewModel", "User profile updated")

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
                                        referralCode = referral,
                                        address = address,
                                        school = school,
                                        ownedVolumeId = listOf(10)
                                    )

                                    db.add(newStudent).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Log.d("SignUpViewModel", "New student data created")

                                            // Deposit to referred user
                                            if (isReferralValid) {
                                                referredUser?.id?.let { referredUserId ->
                                                    transactionService.depositTo(
                                                        referredUserId,
                                                        MaghribMengajiUser.AFFILIATE_REWARD
                                                    ) { transactionResult ->
                                                        transactionResult.onSuccess {
                                                            Log.d("SignUpViewModel", "Deposit to referred user successful")
                                                            studentRepository.setStudent(
                                                                newStudent
                                                            ) // Set newStudent to repository
                                                            _authResult.value =
                                                                Result.success(user) // Return user
                                                            _progressVisibility.value = false
                                                        }.onFailure { e ->
                                                            Log.d("SignUpViewModel", "Deposit to referred user failed")
                                                            _authResult.value =
                                                                Result.failure(e)
                                                            _progressVisibility.value = false
                                                        }
                                                    }
                                                }
                                            } else { // When referral is invalid, return user too
                                                Log.d("SignUpViewModel", "Not deposit to referred user because referral is invalid, return user")
                                                studentRepository.setStudent(
                                                    newStudent
                                                ) // Set newStudent to repository
                                                _authResult.value =
                                                    Result.success(user) // Return user
                                                _progressVisibility.value = false
                                            }

                                        } else {
                                            Log.d("SignUpViewModel", "Failed to create newStudent data")

                                            // Failed to create newStudent data
                                            _authResult.value = Result.failure(
                                                it.exception
                                                    ?: Exception("Failed to create newStudent data")
                                            )
                                            _progressVisibility.value = false
                                        }
                                    }

                                } else {
                                    Log.d("SignUpViewModel", "Failed to update user profile")
                                    // If user profile update fails, display a message to the user.
                                    _authResult.value = Result.failure(
                                        profileUpdateTask.exception
                                            ?: Exception("Failed to update profile")
                                    )
                                    _progressVisibility.value = false
                                }
                            }

                    } else {
                        Log.d("SignUpViewModel", "User is null")
                        // User is null, handle this case if needed
                        _authResult.value = Result.failure(Exception("User is null"))
                        _progressVisibility.value = false
                    }
                } else {
                    Log.d("SignUpViewModel", "Sign up failed")
                    // If sign up fails, display a message to the user.
                    _authResult.value =
                        Result.failure(task.exception ?: Exception("Sign up failed"))
                    _progressVisibility.value = false
                }
            }
    }
}