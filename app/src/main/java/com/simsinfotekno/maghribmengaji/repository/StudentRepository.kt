package com.simsinfotekno.maghribmengaji.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile

class StudentRepository() : Repository<MaghribMengajiUser>() {

    /* Variables */
//    var ustadh: MaghribMengajiUser? = null // Will be filled by retrieveUserProfile(ustadhId) invocation in MainActivity
    private val _ustadhLiveData = MutableLiveData<MaghribMengajiUser>(null)
    val ustadhLiveData: LiveData<MaghribMengajiUser> = _ustadhLiveData

    private val _studentLiveData = MutableLiveData<MaghribMengajiUser>()
    val studentLiveData: LiveData<MaghribMengajiUser> get() = _studentLiveData

    private val retrieveUserProfile = RetrieveUserProfile()

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
//        if (getRecordsCount() < 1) {
            setRecords(
                listOf(
                    student
                ),
                false
            )
//        }

        _studentLiveData.postValue(student)
    }

    fun getStudent(): MaghribMengajiUser? {
        return getRecordByIndex(0)
//        return _studentLiveData.value
    }

    fun fetchStudent() {
        retrieveUserProfile(Firebase.auth.currentUser!!.uid){
            setStudent(it)
        }
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
