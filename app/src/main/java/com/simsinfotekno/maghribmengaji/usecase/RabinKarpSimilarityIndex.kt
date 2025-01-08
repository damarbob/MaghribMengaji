package com.simsinfotekno.maghribmengaji.usecase

import java.text.Normalizer
import kotlin.math.pow
import kotlin.math.round

class RabinKarpSimilarityIndex {

    /**
     * Calculate similarity index of 2 strings
     * using Rabin-Karp method and normalize to a scale
     * of 0 to 100 where 70 is scaled to 100.
     */
    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean both strings by removing diacritics
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        // Calculate the match count using Rabin-Karp string matching
        val rawScore = rabinKarpMatch(cleanStr1, cleanStr2)

        // Normalize the raw score to 0-100
        return normalizeScore(rawScore, cleanStr1.length, cleanStr2.length)
    }

    /**
     * Rabin-Karp string matching to count the number of matching substrings
     * in the two strings. It returns the count of exact matches.
     */
    private fun rabinKarpMatch(str1: String, str2: String): Int {
        val n = str1.length
        val m = str2.length
        val prime = 101
        var matches = 0

        // Edge case: if one string is shorter than the other
        if (n < m) return 0

        // Precompute hash of the substring in str2 and the first substring of str1
        val patternHash = createHash(str2, m, prime)
        var textHash = createHash(str1, m, prime)

        for (i in 0..(n - m)) {
            // Check if hash matches, then compare the substrings directly
            if (patternHash == textHash && str1.substring(i, i + m) == str2) {
                matches++
            }

            // Recompute the hash for the next substring of str1
            if (i < n - m) {
                textHash = recalculateHash(str1, i, i + m, textHash, m, prime)
            }
        }

        return matches
    }

    /**
     * Calculate hash value for a string of length m using rolling hash technique.
     */
    private fun createHash(str: String, length: Int, prime: Int): Int {
        var hash = 0
        for (i in 0 until length) {
            hash += (str[i].code * prime.toDouble().pow(i).toInt())
        }
        return hash
    }

    /**
     * Recalculate hash by removing the old character and adding the new one.
     */
    private fun recalculateHash(str: String, oldIndex: Int, newIndex: Int, oldHash: Int, length: Int, prime: Int): Int {
        var newHash = oldHash - str[oldIndex].code
        newHash /= prime
        newHash += (str[newIndex].code * prime.toDouble().pow(length - 1).toInt())
        return newHash
    }

    /**
     * Normalize the Rabin-Karp match count to a scale of 0 to 100
     * where 70 is scaled to 100.
     */
    private fun normalizeScore(matchCount: Int, length1: Int, length2: Int): Double {
        val maxLength = maxOf(length1, length2)

        // Raw similarity score (based on match count)
        val similarity = matchCount.toDouble() / maxLength

        // Scale the score: 70 becomes 100
        val normalizedScore = similarity * (60.0 / 100.0)

        // Round the score and ensure it doesn't exceed 100
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}