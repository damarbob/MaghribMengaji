package com.simsinfotekno.maghribmengaji.model

data class QuranChapter(
    val id: Int,
    val name: String,
    val volumeIds: List<Int>,
    val pageIds: List<Int>,
)
