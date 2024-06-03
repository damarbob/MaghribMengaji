package com.simsinfotekno.maghribmengaji.model

data class QuranRecordingStudent(
    val studentId: String? = null,
    val pageId: Int? = null,
    var recordingUriString: String? = null
){
    companion object{
        val COLLECTION = "quranRecordingStudents"
    }
}
