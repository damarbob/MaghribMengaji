package com.simsinfotekno.maghribmengaji.usecase

import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranPage

class QuranPageStatusCheck {
    operator fun invoke(
        quranPage: QuranPage,
    ): QuranItemStatus {

        val pageId = quranPage.id

        val record = quranPageStudentRepository.getRecordByPageId(pageId)

        if (record != null) {

            if (record.pictureUriString != null && record.pictureUriString!!.length > 1) {

                return if (record.accuracyScore != null && record.accuracyScore!! > 0) {
                    QuranItemStatus.FINISHED
                } else {
                    QuranItemStatus.ON_PROGRESS
                }

            }

        }

        return QuranItemStatus.NONE

    }
}