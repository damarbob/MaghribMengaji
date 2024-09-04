package com.simsinfotekno.maghribmengaji.model

data class QuranVolumeStudentScore(
    val scoreId: Int? = null,
    val studentId: String? = null,
    val volumeId: Int,
    val quranPageStudent: List<Int>,
    val score: Int? = null,
    val balance: Int? = null,
)
