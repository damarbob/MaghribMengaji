package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class StudentRepository() : Repository<MaghribMengajiUser>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: MaghribMengajiUser) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: MaghribMengajiUser) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: MaghribMengajiUser) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
//        TODO("Not yet implemented")
    }

    fun setStudent(student: MaghribMengajiUser) {
        if (getRecordsCount() < 1) {
            setRecords(
                listOf(
                    student
                ),
                false
            )
        }
    }

    fun getStudent(): MaghribMengajiUser {
        return getRecordByIndex(0)
    }


}
