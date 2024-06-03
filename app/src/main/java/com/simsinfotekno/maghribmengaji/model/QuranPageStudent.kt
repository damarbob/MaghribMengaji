package com.simsinfotekno.maghribmengaji.model

data class QuranPageStudent(
    val pageId: Int? = null,
    val studentId: String? = null,
    var teacherId: String? = null,
    var pictureUriString: String? = null,
    var oCRScore: Int? = null,
    var tidinessScore: Int? = null,
    var accuracyScore: Int? = null,
    var consistencyScore: Int? = null,
) {
    companion object {
        val COLLECTION = "quranPageStudents"
    }
}
