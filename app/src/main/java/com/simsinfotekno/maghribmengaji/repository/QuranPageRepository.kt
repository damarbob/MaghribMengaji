package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.QuranPage

class QuranPageRepository() : Repository<QuranPage>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
        TODO("Not yet implemented")
    }

    fun getRecordById(id: Int?): QuranPage? {
        for (r in getRecords()) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }
}
