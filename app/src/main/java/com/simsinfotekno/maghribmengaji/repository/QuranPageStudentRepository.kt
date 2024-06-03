package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.event.OnPageStudentRepositoryUpdate
import com.simsinfotekno.maghribmengaji.event.OnRepositoryUpdate
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStatusCheck
import org.greenrobot.eventbus.EventBus

class QuranPageStudentRepository() : Repository<QuranPageStudent>() {

    companion object {
        private val TAG = QuranPageStudentRepository::class.java.simpleName
    }

    // Use case
    private val quranPageStatusCheck = QuranPageStatusCheck()

    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranPageStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranPageStudent) {
        EventBus.getDefault().post(
            OnPageStudentRepositoryUpdate(
                OnRepositoryUpdate.Event.ACTION_ADD,
                record
            )
        )
    }

    override fun onRecordDeleted(record: QuranPageStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
//        TODO("Not yet implemented")
    }

    fun getRecordByPageId(id: Int?): QuranPageStudent? {
        for (r in getRecords()) {
            if (r.pageId == id) {
                return r
            }
        }
        return null
    }

    fun getRecordByIds(ids: IntArray): List<QuranPageStudent> {
        val result: ArrayList<QuranPageStudent> = arrayListOf()

        ids.forEach { i ->
            result.add(getRecordByPageId(i)!!)
        }

        return result
    }

    fun getPagesByStatus(quranItemStatus: QuranItemStatus): List<QuranPageStudent> {
        val result = arrayListOf<QuranPageStudent>()

        getRecords().forEach {
            if (it.pictureUriString != null && it.pictureUriString!!.length > 1) {

                if (it.accuracyScore != null && it.accuracyScore!! > 0) {

                    // FINISHED
                    if (quranItemStatus == QuranItemStatus.FINISHED)
                        result.add(it)

                } else {

                    // ON PROGRESS
                    if (quranItemStatus == QuranItemStatus.ON_PROGRESS)
                        result.add(it)

                }

            }
            else {

                // NONE
                if (quranItemStatus == QuranItemStatus.NONE)
                    result.add(it)

            }
        }

        return result
    }


}
