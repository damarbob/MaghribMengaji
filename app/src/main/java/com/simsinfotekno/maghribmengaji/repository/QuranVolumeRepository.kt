package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.QuranVolume

class QuranVolumeRepository() : Repository<QuranVolume>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranVolume) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranVolume) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: QuranVolume) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
        TODO("Not yet implemented")
    }

    fun getRecordById(id: Int?): QuranVolume? {
        for (r in getRecords()) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }

    fun getRecordsById(ids: IntArray): List<QuranVolume> {
        val result: ArrayList<QuranVolume> = arrayListOf()

        ids.forEach { i ->
            result.add(getRecordById(i)!!)
        }

        return result
    }

    fun getRecordByPageId(pageId: Int): QuranVolume? {
        for (r in getRecords()) {
            r.pageIds.forEach {
                if (it == pageId) {
                    return r
                }
            }
        }
        return null
    }
}
