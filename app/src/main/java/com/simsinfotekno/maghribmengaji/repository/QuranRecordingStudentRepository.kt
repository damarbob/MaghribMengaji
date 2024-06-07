package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.event.OnRecordingStudentRepositoryUpdate
import com.simsinfotekno.maghribmengaji.event.OnRepositoryUpdate
import com.simsinfotekno.maghribmengaji.model.QuranRecordingStudent
import org.greenrobot.eventbus.EventBus

class QuranRecordingStudentRepository(): Repository<QuranRecordingStudent>() {
    override fun onStart() {
        // Register to EventBus or any other initialization if needed
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        // Unregister from EventBus or any other cleanup if needed
        EventBus.getDefault().unregister(this)
    }

    override fun onRecordCleared() {
        EventBus.getDefault().post(
            OnRecordingStudentRepositoryUpdate(
                OnRepositoryUpdate.Event.ACTION_CLEAR,
                null
            )
        )
    }

    override fun onRecordDeleted(record: QuranRecordingStudent) {
        EventBus.getDefault().post(
            OnRecordingStudentRepositoryUpdate(
                OnRepositoryUpdate.Event.ACTION_DELETE,
                record
            )
        )
    }

    override fun onRecordAdded(record: QuranRecordingStudent) {
        EventBus.getDefault().post(
            OnRecordingStudentRepositoryUpdate(
                OnRepositoryUpdate.Event.ACTION_ADD,
                record
            )
        )
    }

    override fun createRecord(record: QuranRecordingStudent) {
        addRecord(record)
    }

    fun getRecordByStudentIdAndPageId(studentId: String, pageId: Int): QuranRecordingStudent? {
        for (r in getRecords()) {
            if (r.studentId == studentId && r.pageId == pageId) {
                return r
            }
        }
        return null
    }

    fun getRecordsByStudentId(studentId: String): List<QuranRecordingStudent> {
        return getRecords().filter { it.studentId == studentId }
    }

    fun getRecordsByPageId(pageId: Int): List<QuranRecordingStudent> {
        return getRecords().filter { it.pageId == pageId }
    }
}