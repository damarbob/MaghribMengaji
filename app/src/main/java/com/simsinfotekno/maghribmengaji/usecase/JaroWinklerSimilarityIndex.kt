import com.simsinfotekno.maghribmengaji.usecase.RemoveDiacritics

import kotlin.math.min
import kotlin.math.round

class JaroWinklerSimilarityIndex {
    /**
     * Calculate similarity index of 2 strings
     * using Jaro-Winkler Distance method and normalize
     * to a scale of 0 to 100 where 70 is scaled to 100.
     */
    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean both strings by removing diacritics
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        // Calculate the raw Jaro-Winkler distance
        val rawScore = jaroWinklerDistance(cleanStr1, cleanStr2)

        // Normalize the raw score to 0-100
        return normalizeScore(rawScore, cleanStr1.length, cleanStr2.length)
    }

    /**
     * Calculate the Jaro-Winkler Distance between two strings.
     */
    private fun jaroWinklerDistance(str1: String, str2: String): Double {
        val mtp = matches(str1, str2)
        val m = mtp[0].toDouble()

        if (m == 0.0) return 0.0

        val j = (m / str1.length + m / str2.length + (m - mtp[1]) / m) / 3.0
        val jw = if (j < 1.0) j else j + min(0.1, 1.0 / mtp[3]) * mtp[2] * (1 - j)

        return jw
    }

    /**
     * Helper function to calculate the number of matching characters,
     * transpositions, and prefix length for Jaro-Winkler Distance.
     */
    private fun matches(str1: String, str2: String): IntArray {
        val maxDist = (maxOf(str1.length, str2.length) / 2) - 1
        val s1Matches = BooleanArray(str1.length)
        val s2Matches = BooleanArray(str2.length)

        var matches = 0
        var transpositions = 0
        var prefix = 0

        // Check for matches
        for (i in str1.indices) {
            val start = maxOf(0, i - maxDist)
            val end = min(str2.length - 1, i + maxDist)

            for (j in start..end) {
                if (s1Matches[i] || s2Matches[j] || str1[i] != str2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return intArrayOf(0, 0, 0, maxOf(str1.length, str2.length))

        // Check for transpositions
        var k = 0
        for (i in str1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (str1[i] != str2[k]) transpositions++
            k++
        }

        // Check for common prefix
        for (i in 0 until min(4, str1.length)) {
            if (str1[i] == str2[i]) prefix++ else break
        }

        return intArrayOf(matches, transpositions / 2, prefix, str1.length)
    }

    /**
     * Normalize the Jaro-Winkler index to a scale of 0 to 100
     * where 70 is scaled to 100.
     */
    private fun normalizeScore(rawScore: Double, length: Int, length1: Int): Double {
        // Normalize to a 0-100 scale where 70 maps to 100
        val normalizedScore = rawScore * (100.0 / 100.0)

        // Round and return the normalized score
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}