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

    fun getRecordByPageId(pageId: Int): List<QuranChapter> {
        val result: ArrayList<QuranChapter> = arrayListOf()
        for (r in getRecords()) {
            if (r.pageIds.contains(pageId)) {
                result.add(r)
            }
        }
        return result
    }

    fun getRecordByPageIds(pageIds: IntArray): List<QuranChapter> {
        val result: HashSet<QuranChapter> = hashSetOf()

        pageIds.forEach { pageId ->
            result.addAll(getRecordByPageId(pageId))
        }

        return result.toList()
    }
}
