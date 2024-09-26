package com.simsinfotekno.maghribmengaji.usecase

import kotlin.math.round

    /**
     * Calculate similarity index of 2 strings
     * with Jaccard method and normalize to a scale
     * of 0 to 100 where 70 is scaled to 100.
     */
class JaccardSimilarityIndex {

    operator fun invoke(str1: String, str2: String): Double {
        val removeDiacritics = RemoveDiacritics()

        // Clean and convert strings to sets
        val cleanStr1 = removeDiacritics(str1).toSet()
        val cleanStr2 = removeDiacritics(str2).toSet()

        // Calculate intersection and union sizes
        val intersectionSize = cleanStr1.intersect(cleanStr2).size.toDouble()
        val unionSize = cleanStr1.union(cleanStr2).size.toDouble()

        // Calculate the raw Jaccard index
        val rawScore = intersectionSize / unionSize

        // Normalize the raw score
        return normalizeScore(rawScore)
//        return rawScore
    }

    /**
     * Normalize the Jaccard index to a scale of 0 to 100
     * where 90 is scaled to 100.
     */
    private fun normalizeScore(rawScore: Double): Double {
        // Normalize to a 0-100 scale where 90 maps to 100
        val normalizedScore = ((rawScore) / (90.0 / 100.0))

        // Round and return the normalized score
        return if (round(normalizedScore * 100) <= 100) round(normalizedScore * 100) else 100.0
    }
}
