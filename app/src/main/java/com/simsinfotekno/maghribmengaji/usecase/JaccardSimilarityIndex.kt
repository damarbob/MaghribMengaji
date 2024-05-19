package com.simsinfotekno.maghribmengaji.usecase

import kotlin.math.round

class JaccardSimilarityIndex {

    /**
     * Calculate similarity index of 2 strings
     * with Jaccard method
     */
    operator fun invoke(str1: String, str2: String): Double {

        val removeDiacritics = RemoveDiacritics()

        val cleanStr1 = removeDiacritics(str1).toSet()
        val cleanStr2 = removeDiacritics(str2).toSet()

        val intersectionSize = cleanStr1.intersect(cleanStr2).size.toDouble()
        val unionSize = cleanStr1.union(cleanStr2).size.toDouble()

        return round(intersectionSize / unionSize * 1000) / 10
    }
}