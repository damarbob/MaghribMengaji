package com.simsinfotekno.maghribmengaji.model

import android.graphics.Bitmap

data class QuranPage(
    val id: Int,
    val name: String,
    val picture: Bitmap,
    val volumeIds: List<Int>,
    val chapterIds: List<Int>,
)
