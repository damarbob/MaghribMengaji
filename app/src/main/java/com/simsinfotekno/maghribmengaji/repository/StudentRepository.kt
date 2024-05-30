package com.simsinfotekno.maghribmengaji.repository

import com.simsinfotekno.maghribmengaji.model.MaghribMengajiStudent

class StudentRepository() : Repository<MaghribMengajiStudent>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: MaghribMengajiStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: MaghribMengajiStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: MaghribMengajiStudent) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
//        TODO("Not yet implemented")
    }

    fun setStudent(student: MaghribMengajiStudent) {
        if (getRecordsCount() < 1) {
            setRecords(
                listOf(
                    student
                ),
                false
            )
        }
    }

    fun getStudent(): MaghribMengajiStudent {
        return getRecordByIndex(0)
    }


}
