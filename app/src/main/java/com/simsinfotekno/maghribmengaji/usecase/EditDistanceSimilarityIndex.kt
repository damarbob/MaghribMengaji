package com.simsinfotekno.maghribmengaji.usecase

import kotlin.math.round

class EditDistanceSimilarityIndex {

    /**
     * Calculate similarity index of 2 strings
     * using Edit Distance (Levenshtein Distance) method
     * and normalize to a scale of 0 to 100 where 70 is scaled to 100.
     */
    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean both strings by removing diacritics
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        // Calculate the raw edit distance
        val rawScore = levenshteinDistance(cleanStr1, cleanStr2)

        // Normalize the raw score to 0-100
        //return normalizeScore(rawScore, cleanStr1.length, cleanStr2.length)
        return rawScore.toDouble()

    }

    /**
     * Calculate Levenshtein Distance between two strings.
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val n = str1.length
        val m = str2.length

        // Edge case: if either string is empty
        if (n == 0) return m
        if (m == 0) return n

        // Create a 2D array for storing edit distances
        val dp = Array(n + 1) { IntArray(m + 1) }

        // Initialize the base cases
        for (i in 0..n) dp[i][0] = i
        for (j in 0..m) dp[0][j] = j

        // Fill the DP array with edit distances
        for (i in 1..n) {
            for (j in 1..m) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,    // Deletion
                    dp[i][j - 1] + 1,    // Insertion
                    dp[i - 1][j - 1] + cost // Substitution
                )
            }
        }

        // Return the edit distance between the two strings
        return dp[n][m]
    }

    /**
     * Normalize the Edit Distance to a scale of 0 to 100
     * where 70 is scaled to 100.
     */

    private fun normalizeScore(editDistance: Int, length1: Int, length2: Int): Double {
        val maxLength = maxOf(length1, length2)

        // Raw similarity score (inverted distance)
        val similarity = 1.0 - (editDistance.toDouble() / maxLength)

        // Scale the score: 70 becomes 100
        val normalizedScore = similarity * (100.0 / 70.0)

        // Round the score and ensure it doesn't exceed 100
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}