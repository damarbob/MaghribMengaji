package com.simsinfotekno.maghribmengaji.model

data class QuranVolume(
    val id: Int,
    val name: String,
    val pageIds: List<Int> = listOf(),
    val chapterIds: List<Int> = listOf(),
)
