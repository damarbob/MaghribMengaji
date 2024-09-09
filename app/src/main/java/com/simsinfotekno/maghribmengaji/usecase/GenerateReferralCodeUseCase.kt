package com.simsinfotekno.maghribmengaji.usecase

import android.util.Base64

class GenerateReferralCodeUseCase {
    operator fun invoke(uid: String, onResult: (Result<String>) -> Unit) {
        // Get the current time in milliseconds
        val currentTimeMillis = System.currentTimeMillis()

        // Convert the current time to a base-36 string
        val timeComponent = currentTimeMillis.toString(36).uppercase()

        // Shorten the uid by encoding it in Base64, then remove padding characters
        val shortenedUid = Base64.encodeToString(uid.toByteArray(), Base64.NO_PADDING or Base64.NO_WRAP).take(4).uppercase()

        // Combine the time component and shortened uid to form the referral code
        onResult(Result.success("$timeComponent$shortenedUid"))
    }
}