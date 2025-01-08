package com.simsinfotekno.maghribmengaji.usecase

import kotlin.math.round

/**
 * Calculate similarity index of 2 strings
 * with Jaro method and normalize to a scale
 * of 0 to 100 where 70 is scaled to 100.
 */
/**
 * Note that the Jaro similarity calculation is more complex than the Jaccard similarity calculation,
 * and it requires iterating over the characters of both strings to calculate the number of common characters.
 */
class JaroSimilarityIndex {

    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean strings
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        // Calculate Jaro similarity
        val m = commonCharacters(cleanStr1, cleanStr2)
        val t = (m / 2.0) * (m / 2.0) * (m / 2.0) * (m / 2.0)
        val p1 = m.toDouble() / cleanStr1.length
        val p2 = m.toDouble() / cleanStr2.length
        val jaroScore = (m.toDouble() / cleanStr1.length + m.toDouble() / cleanStr2.length + t) / 2.0

        // Normalize the Jaro score
        return normalizeScore(jaroScore)
        //nonactive for testing
        //return jaroScore
    }

    /**
     * Calculate the number of common characters between two strings
     */
    private fun commonCharacters(str1: String, str2: String): Int {
        val str1Array = str1.toCharArray()
        val str2Array = str2.toCharArray()
        var m = 0
        var i = 0
        var j = 0
        var k = 0
        val matchIndexes = BooleanArray(str2Array.size)

        while (i < str1Array.size) {
            k = maxOf(1, i - 2)
            k = minOf(k, str2Array.size - 1)
            j = i
            while (j < str1Array.size && k < str2Array.size) {
                if (!matchIndexes[k] && str1Array[j] == str2Array[k]) {
                    m++
                    matchIndexes[k] = true
                    break
                }
                k++
            }
            i++
        }
        return m
    }
    /**
     * Normalize the Jaro index to a scale of 0 to 100
     * where 90 is scaled to 100.
     */
    private fun normalizeScore(rawScore: Double): Double {
        // Normalize to a 0-100 scale where 90 maps to 100
        val normalizedScore = ((rawScore) / (70.0 / 100.0))

        // Round and return the normalized score
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}