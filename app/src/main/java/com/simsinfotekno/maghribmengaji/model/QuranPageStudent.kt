package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class QuranPageStudent(
    val pageId: Int? = null,
    val studentId: String? = null,
    var teacherId: String? = null,
    var pictureUriString: String? = null,
    var oCRScore: Int? = null,
    var tidinessScore: Int? = null,
    var accuracyScore: Int? = null,
    var consistencyScore: Int? = null,
    var createdAt: Timestamp? = null
) {
    companion object {
        val COLLECTION = "quranPageStudents"
    }
}
