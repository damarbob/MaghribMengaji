package com.simsinfotekno.maghribmengaji.model

data class QuranPage(
    val id: Int,
    val name: String,
    val pictureUrl: String? = null,
    val volumeId: Int? = null,
    val chapterIds: List<Int> = listOf(),
) {
    companion object {
        val COLLECTION = "quranPages"
    }
}
