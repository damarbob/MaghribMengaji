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

        val resultSet = mutableSetOf<QuranVolume>()  // Use a set to store unique volumes

        Log.d(TAG, "Starting $TAG")

        val pages = quranPageStudentRepository.getPagesByStatus(quranItemStatus)
        Log.d(TAG, pages.toString())

        pages.forEach { page ->
            val volumeId = quranPageRepository.getRecordById(page.pageId)?.volumeId

            val volume = quranVolumeRepository.getRecordById(volumeId)
            if (volume != null) {
                resultSet.add(volume) // Set automatically handles duplicates
            }

            Log.d(TAG, "Found volume $volumeId")
        }

        Log.d(TAG, "Ending $TAG")

        return resultSet.toList() // Convert set back to list

    }

}