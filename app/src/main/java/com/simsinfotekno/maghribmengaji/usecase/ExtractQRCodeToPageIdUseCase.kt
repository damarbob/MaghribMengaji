package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log

class ExtractQRCodeToPageIdUseCase {
    companion object {
        val TAG = ExtractQRCodeToPageIdUseCase::class.simpleName
        const val PAGE_ID_NOT_SAME = 1001
        const val PAGE_ID_NOT_FOUND = 1000
    }

    operator fun invoke(input: String): Int? {

        // Define the regex pattern to match the number after "Halaman" and before the next underscore
        val regex = """Halaman(\d+)_""".toRegex()

        // Find the match in the input string
        val matchResult = regex.find(input)

        return if (matchResult != null) {
            // Extract the number part from the match result
            val numberString = matchResult.groupValues[1]
            // Convert the number string to an integer
            Log.d(TAG, "Number: $numberString")
            numberString.toIntOrNull()
        } else {
            // Return null if no match is found
            null
        }

    }
}