package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.MaghribMengajiTransaction

class TransactionRepository() : Repository<MaghribMengajiTransaction>() {

    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: MaghribMengajiTransaction) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: MaghribMengajiTransaction) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: MaghribMengajiTransaction) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
//        TODO("Not yet implemented")
    }

    fun getRecordById(id: String): MaghribMengajiTransaction? {
        for (r in getRecords()) {
            if (r.id?.equals(id) == true) {
                return r
            }
        }
        return null
    }

    fun getBalance(): Int? {
        return getRecords().maxByOrNull { it.createdAt!! }?.balance
    }

}
