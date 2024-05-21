package com.simsinfotekno.maghribmengaji.model

data class QuranPageStudent(
    val pageId: Int,
    val studentId: Int,
    val teacherId: Int,
    val pictureUrl: String?,
    val OCRScore: Int?,
    val tidinessScore: Int?,
    val accuracyScore: Int?,
    val consistencyScore: Int?,
)
