package com.simsinfotekno.maghribmengaji.model

import android.graphics.Bitmap

data class QuranPage(
    val id: Int,
    val name: String,
    val picture: Bitmap? = null, // TODO: Delete soon
    val pictureUrl: String? = null,
    val volumeId: Int? = null,
    val chapterIds: List<Int> = listOf(),
) {
    companion object {
        val COLLECTION = "quranPages"
    }
}
