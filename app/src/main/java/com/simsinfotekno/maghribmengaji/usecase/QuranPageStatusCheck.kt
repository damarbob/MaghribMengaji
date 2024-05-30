package com.simsinfotekno.maghribmengaji.usecase

import com.simsinfotekno.maghribmengaji.model.MaghribMengajiStudent

class QuranPageStatusCheck(
    private val maghribMengajiStudent: MaghribMengajiStudent
) {
//    operator fun invoke(
//        quranPage: QuranPage,
//    ): QuranItemStatus {
//
//        val pageId = quranPage.id
//
//        maghribMengajiStudent.finishedPageIds?.forEach {
//            if (it == pageId)
//                return QuranItemStatus.FINISHED
//        }
//
//        maghribMengajiStudent.onProgressPageIds?.forEach {
//            if (it == pageId)
//                return QuranItemStatus.ON_PROGRESS
//        }
//
//        return QuranItemStatus.NONE
//
//    }
}