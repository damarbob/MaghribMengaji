package com.simsinfotekno.maghribmengaji.usecase

import kotlin.math.round

class BruteForceSimilarityIndex {
    /**
     * Calculate similarity index of 2 strings
     * using Brute Force method and normalize to a scale
     * of 0 to 100 where 70 is scaled to 100.
     */
    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean both strings by removing diacritics
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        // Calculate the match count using brute force string matching
        val rawScore = bruteForceMatch(cleanStr1, cleanStr2)

        // Normalize the raw score to 0-100
        return normalizeScore(rawScore, cleanStr1.length, cleanStr2.length)
    }

    /**
     * Brute force string matching to count the number of matching characters
     * in the two strings. It returns the count of exact matches.
     */
    private fun bruteForceMatch(str1: String, str2: String): Int {
        val n = str1.length
        val m = str2.length
        var matchCount = 0

        // Compare all substrings of length m in str1 with str2
        for (i in 0..(n - m)) {
            if (str1.substring(i, i + m) == str2) {
                matchCount++
            }
        }

        return matchCount
    }

    /**
     * Normalize the Brute Force match count to a scale of 0 to 100
     * where 70 is scaled to 100.
     */
    private fun normalizeScore(matchCount: Int, length1: Int, length2: Int): Double {
        val maxLength = maxOf(length1, length2)

        // Raw similarity score (based on match count)
        val similarity = matchCount.toDouble() / maxLength

        // Scale the score: 70 becomes 100
        val normalizedScore = similarity * (100.0 / 70.0)

        // Round the score and ensure it doesn't exceed 100
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}