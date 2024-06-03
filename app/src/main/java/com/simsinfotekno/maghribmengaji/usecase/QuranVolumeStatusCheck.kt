//package com.simsinfotekno.maghribmengaji.usecase
//
//import com.simsinfotekno.maghribmengaji.MainApplication
//import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
//import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
//import com.simsinfotekno.maghribmengaji.model.QuranPage
//import com.simsinfotekno.maghribmengaji.model.QuranVolume
//import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
//
//class QuranVolumeStatusCheck {
//
//    private lateinit var quranPageStatusCheck: QuranPageStatusCheck
//    operator fun invoke(
//        quranVolume: QuranVolume,
//    ): QuranItemStatus {
//
//        quranPageStatusCheck = QuranPageStatusCheck()
//
////        val pageIds = quranVolume.pageIds.toIntArray()
//        val pageIds = arrayListOf<Int>()
//        quranVolume.pageIds.forEach {
//            val pageStudent = quranPageStudentRepository.getRecordByPageId(it)
//            if (pageStudent != null) {
//                pageStudent.pageId?.let { it1 -> pageIds.add(it1) }
//            }
//        }
//
//        val records = quranPageStudentRepository.getRecordByIds(pageIds.toIntArray())
//
//        if (records.size >= 20) {
//
//            for (record in records) {
//                val quranPage = QuranPageRepository().getRecordById(record.pageId)
//                if (quranPage != null) {
//                    val status = quranPageStatusCheck(quranPage)
//
//                    return if (status == QuranItemStatus.ON_PROGRESS) {
//                        QuranItemStatus.ON_PROGRESS
//                    } else if (status == QuranItemStatus.NONE) {
//                        QuranItemStatus.NONE
//                    } else continue
//
//                }
//            }
//
//            return QuranItemStatus.FINISHED
//
//        } else {
//            for (record in records) {
//                val quranPage = QuranPageRepository().getRecordById(record.pageId)
//                if (quranPage != null) {
//                    val status = quranPageStatusCheck(quranPage)
//
//                    return if (status == QuranItemStatus.ON_PROGRESS) {
//                        QuranItemStatus.ON_PROGRESS
//                    } else if (status == QuranItemStatus.NONE) {
//                        QuranItemStatus.NONE
//                    } else continue
//
//                }
//            }
//        }
//        return QuranItemStatus.NONE
//    }
//}

package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume

class QuranVolumeStatusCheck {

    companion object {
        private val TAG = QuranVolumeStatusCheck::class.java.simpleName
    }

    private val quranPageStatusCheck = QuranPageStatusCheck()

    operator fun invoke(quranVolume: QuranVolume): QuranItemStatus {
        val pageIds = quranVolume.pageIds.mapNotNull {
            quranPageStudentRepository.getRecordByPageId(it)?.pageId
        }

        val records = quranPageStudentRepository.getRecordByIds(pageIds.toIntArray())

        var finishedCount = 0
        var noneCount = 0

        records.forEach { record ->
            val quranPage = quranPageRepository.getRecordById(record.pageId)
            quranPage?.let {
                when (quranPageStatusCheck(it)) {
                    QuranItemStatus.ON_PROGRESS -> return QuranItemStatus.ON_PROGRESS
                    QuranItemStatus.FINISHED -> finishedCount++
                    QuranItemStatus.NONE -> noneCount++
                }
            }
        }

        return when {
            finishedCount >= 20 -> QuranItemStatus.FINISHED
            noneCount == records.size -> QuranItemStatus.NONE
            else -> QuranItemStatus.NONE // Default if no criteria met
        }
    }
}
