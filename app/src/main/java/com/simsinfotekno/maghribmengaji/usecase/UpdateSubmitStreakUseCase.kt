package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class UpdateSubmitStreakUseCase {
    companion object {
        val TAG = UpdateSubmitStreakUseCase::class.simpleName
    }

    private val updateUserProfile = UpdateUserProfile()

    suspend operator fun invoke(onSubmitStreakUpdated: (Result<Map<String, Any>>) -> Unit) {
        val student = MainApplication.studentRepository.getStudent()

        val lastDailySubmitMillis = student?.lastDailySubmit?.toDate()?.time
        val currentStreak = student?.currentSubmitStreak ?: 1 // Initialize streak to 1 if null

        val today = Calendar.getInstance()
        val lastSubmitDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
            lastDailySubmitMillis?.let { timeInMillis = it }
        }

        Log.d(TAG, "current streak: $currentStreak")
        Log.d(TAG, "last daily submit: $lastSubmitDate")

        // If the user already submitted today, do nothing
        if (isSameDay(today, lastSubmitDate)) {
            Log.d(TAG, "User already submitted today.")
            onSubmitStreakUpdated(Result.success(emptyMap()))
        }

        val updatedData = hashMapOf(
            "lastDailySubmit" to Timestamp.now(),
            "currentSubmitStreak" to if (isYesterday(lastSubmitDate, today)) {
                currentStreak + 1 // Increment streak
            } else {
                1 // Reset streak
            }
        )

        updateUserProfile(Firebase.auth.currentUser!!.uid, updatedData) { result ->
            result.onSuccess {
                onSubmitStreakUpdated(Result.success(updatedData))
            }
            result.onFailure { exception ->
                Log.e(TAG, "Error updating streak: ", exception)
                onSubmitStreakUpdated(Result.failure(exception))
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        cal2.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(cal1, cal2)
    }
}