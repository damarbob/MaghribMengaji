package com.simsinfotekno.maghribmengaji.model

import android.graphics.Bitmap

data class QuranPage(
    val id: Int,
    val name: String,
    val picture: Bitmap? = null,
    val volumeIds: List<Int> = listOf(),
    val chapterIds: List<Int> = listOf(),
)
