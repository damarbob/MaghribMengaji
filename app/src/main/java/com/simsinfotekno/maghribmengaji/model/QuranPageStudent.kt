package com.simsinfotekno.maghribmengaji.model

import android.graphics.Bitmap

data class QuranPageStudent(
    val pageId: Int,
    val studentId: String,
    var teacherId: String? = null,
    var pictureUrl: String? = null,
    var picture: Bitmap? = null,
    var OCRScore: Int? = null,
    var tidinessScore: Int? = null,
    var accuracyScore: Int? = null,
    var consistencyScore: Int? = null,
)
