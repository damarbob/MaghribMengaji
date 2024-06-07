package com.simsinfotekno.maghribmengaji.event

import com.simsinfotekno.maghribmengaji.model.QuranRecordingStudent

class OnRecordingStudentRepositoryUpdate: OnRepositoryUpdate<QuranRecordingStudent?> {
    private var recordingStudent: QuranRecordingStudent? = null

    constructor(event: Event) : super(event)
    constructor(event: Event, recordingStudent: QuranRecordingStudent?) : super(event) {
        this.recordingStudent = recordingStudent
    }
}