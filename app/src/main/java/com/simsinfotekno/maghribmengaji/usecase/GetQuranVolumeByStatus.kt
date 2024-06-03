package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume

class GetQuranVolumeByStatus {

    companion object {
        private val TAG = GetQuranVolumeByStatus::class.java.simpleName
    }

    operator fun invoke(quranItemStatus: QuranItemStatus): List<QuranVolume> {

        val result = arrayListOf<QuranVolume>()

        Log.d(TAG, "Starting $TAG")

        val pages = quranPageStudentRepository.getPagesByStatus(quranItemStatus)
        Log.d(TAG, pages.toString())

        pages.forEach {
            val volumeId = quranPageRepository.getRecordById(it.pageId)?.volumeId

            quranVolumeRepository.getRecordById(volumeId)?.let { volume -> result.add(volume) }

            Log.d(TAG, "Found volume $volumeId")
        }

        Log.d(TAG, "Ending $TAG")

        return result

    }

}