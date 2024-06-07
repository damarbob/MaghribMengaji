package com.simsinfotekno.maghribmengaji.usecase

import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent

class QuranPageStudentStatusCheck {
    operator fun invoke(
        quranPageStudent: QuranPageStudent,
    ): QuranItemStatus {

            if (quranPageStudent.pictureUriString != null && quranPageStudent.pictureUriString!!.length > 1) {

                return if (quranPageStudent.accuracyScore != null && quranPageStudent.accuracyScore!! > 0) {
                    QuranItemStatus.FINISHED
                } else {
                    QuranItemStatus.ON_PROGRESS
                }

            }

        return QuranItemStatus.NONE

    }
}