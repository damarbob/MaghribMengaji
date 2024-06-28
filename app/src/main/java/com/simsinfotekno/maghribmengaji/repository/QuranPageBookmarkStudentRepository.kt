package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.QuranPageBookmarkStudent

class QuranPageBookmarkStudentRepository() : Repository<QuranPageBookmarkStudent>() {

    companion object {
        private val TAG = QuranPageBookmarkStudentRepository::class.java.simpleName
    }

    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranPageBookmarkStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranPageBookmarkStudent) {
        //
    }

    override fun onRecordDeleted(record: QuranPageBookmarkStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
//        TODO("Not yet implemented")
    }

    fun getRecordByPageId(id: Int?): QuranPageBookmarkStudent? {
        for (r in getRecords()) {
            if (r.pageId == id) {
                return r
            }
        }
        return null
    }

    fun getRecordByIds(ids: IntArray): List<QuranPageBookmarkStudent> {
        val result: ArrayList<QuranPageBookmarkStudent> = arrayListOf()

        ids.forEach { i ->
            result.add(getRecordByPageId(i)!!)
        }

        return result
    }

    fun getRecordByIdsNoStrict(ids: IntArray): List<QuranPageBookmarkStudent> {
        val result: ArrayList<QuranPageBookmarkStudent> = arrayListOf()

        ids.forEach { i ->
            getRecordByPageId(i)?.let { result.add(it) }
        }

        return result
    }

}
