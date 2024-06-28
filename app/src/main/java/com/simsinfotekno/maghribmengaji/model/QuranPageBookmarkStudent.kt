package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class QuranPageBookmarkStudent(
    var pageId: Int? = null,
    var name: String? = null,
    var studentId: String? = null,
    var createdAt: Timestamp? = null,
) {
    companion object {
        val COLLECTION = "quranPageBookmarkStudents"
    }
}