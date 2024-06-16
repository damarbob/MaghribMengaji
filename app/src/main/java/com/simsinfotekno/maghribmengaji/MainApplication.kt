package com.simsinfotekno.maghribmengaji

import android.app.Application
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranRecordingStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranVolumeRepository
import com.simsinfotekno.maghribmengaji.repository.StudentRepository

class MainApplication: Application() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName

        /* Repositories */

        // User repos
        val quranVolumeRepository = QuranVolumeRepository()
        val quranPageRepository = QuranPageRepository()
        val quranPageStudentRepository = QuranPageStudentRepository()
        val studentRepository = StudentRepository()
        val quranRecordingStudentRepository = QuranRecordingStudentRepository()

        // Ustadh repos
        val ustadhStudentRepository = StudentRepository()
        val ustadhQuranVolumeStudentRepository = QuranVolumeRepository()
        val ustadhQuranPageStudentRepository = QuranPageStudentRepository()

        /* Data set preparation */
        // Volume and pages
        val quranVolumes = listOf(
            QuranVolume(1, "1", (1..61).toList()),
            QuranVolume(2, "2", (62..121).toList()),
            QuranVolume(3, "3", (122..181).toList()),
            QuranVolume(4, "4", (182..241).toList()),
            QuranVolume(5, "5", (242..301).toList()),
            QuranVolume(6, "6", (302..361).toList()),
            QuranVolume(7, "7", (362..421).toList()),
            QuranVolume(8, "8", (422..481).toList()),
            QuranVolume(9, "9", (482..541).toList()),
            QuranVolume(10, "10", (542..604).toList()),
        )
        val quranPages = (1..604).map { pageId ->
            // Find the volume for this page
            val volumeId = quranVolumes.find { volume ->
                pageId in volume.pageIds
            }?.id ?: throw IllegalArgumentException("Page $pageId does not belong to any volume")

            QuranPage(pageId, "$pageId", volumeId = volumeId)
        }

    }
    override fun onCreate() {
        super.onCreate()

        // Insert initial data set to repository
        quranVolumeRepository.setRecords(quranVolumes, false)
        quranPageRepository.setRecords(quranPages, false)

    }
}