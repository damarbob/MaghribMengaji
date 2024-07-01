package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.QuranChapter

class QuranChapterRepository() : Repository<QuranChapter>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranChapter) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranChapter) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: QuranChapter) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
        TODO("Not yet implemented")
    }

    fun getRecordById(id: Int?): QuranChapter? {
        for (r in getRecords()) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }

    fun getRecordByIds(ids: IntArray): List<QuranChapter> {
        val result: ArrayList<QuranChapter> = arrayListOf()

        ids.forEach { i ->
            result.add(getRecordById(i)!!)
        }

        return result
    }

}
