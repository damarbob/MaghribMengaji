package com.simsinfotekno.maghribmengaji.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class StudentRepository() : Repository<MaghribMengajiUser>() {

    /* Variables */
//    var ustadh: MaghribMengajiUser? = null // Will be filled by retrieveUserProfile(ustadhId) invocation in MainActivity
    private val _ustadhLiveData = MutableLiveData<MaghribMengajiUser>(null)
    val ustadhLiveData: LiveData<MaghribMengajiUser> = _ustadhLiveData

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

    fun getStudent(): MaghribMengajiUser? {
        return getRecordByIndex(0)
    }

    fun getStudentById(studentId: String): MaghribMengajiUser? {
        for (r in getRecords()) {
            if (r.id?.equals(studentId) == true) {
                return r
            }
        }
        return null
    }

    fun setUstadh(ustadh: MaghribMengajiUser) {
        _ustadhLiveData.value = ustadh
    }

    fun getUstadh(): MaghribMengajiUser? {
        return _ustadhLiveData.value
    }

}
