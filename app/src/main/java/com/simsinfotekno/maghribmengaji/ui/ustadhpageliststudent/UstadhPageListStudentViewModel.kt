package com.simsinfotekno.maghribmengaji.ui.ustadhpageliststudent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStudentStatusCheck

class UstadhPageListStudentViewModel : ViewModel() {

    /* Variables */
    var volumeId: Int? = null
    var pages: List<QuranPageStudent> = listOf()
    val _pagesLiveData = MutableLiveData<List<QuranPageStudent>>()

    /* Use cases */
    val quranPageStudentStatusCheck = QuranPageStudentStatusCheck()

    fun init(volumeId: Int) {
        this.volumeId = volumeId
        _pagesLiveData.value = quranVolumeRepository.getRecordById(volumeId)?.pageIds?.toIntArray()?.let {
            MainApplication.ustadhQuranPageStudentRepository.getRecordByIdsNoStrict(it)
        } ?: listOf() // Show pages within the volume otherwise show nothing
    }

    private fun getPagesByStatus(status: QuranItemStatus) {
        _pagesLiveData.value = getRepositoryRecords().filter { quranPageStudentStatusCheck(it) == status }.toMutableList()
    }

    private fun getRepositoryRecords(): List<QuranPageStudent> {
        return quranVolumeRepository.getRecordById(volumeId)?.pageIds?.toIntArray()?.let {
            MainApplication.ustadhQuranPageStudentRepository.getRecordByIdsNoStrict(it)
        } ?: listOf() // Show pages within the volume otherwise show nothing
    }

    fun getAll() {
        _pagesLiveData.value = getRepositoryRecords()
    }

    fun getOnProgress() {
        getPagesByStatus(QuranItemStatus.ON_PROGRESS)
    }

    fun getFinished() {
        getPagesByStatus(QuranItemStatus.FINISHED)
    }

}