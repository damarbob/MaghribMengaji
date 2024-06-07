package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class QuranRecordingStudent(
    val studentId: String? = null,
    val pageId: Int? = null,
    var recordingUriString: String? = null,
    var recordingFilename: String? = null,
    var createdAt: Timestamp? = null,
){
    companion object{
        val COLLECTION = "quranRecordingStudents"
    }
}
