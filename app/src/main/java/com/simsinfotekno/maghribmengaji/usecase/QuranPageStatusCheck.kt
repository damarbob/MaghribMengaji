package com.simsinfotekno.maghribmengaji.usecase

import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPage

class QuranPageStatusCheck(
    private val maghribMengajiUser: MaghribMengajiUser
) {
    operator fun invoke(
        quranPage: QuranPage,
    ): QuranItemStatus {

        val pageId = quranPage.id

        maghribMengajiUser.finishedPageIds?.forEach {
            if (it == pageId)
                return QuranItemStatus.FINISHED
        }

        maghribMengajiUser.onProgressPageIds?.forEach {
            if (it == pageId)
                return QuranItemStatus.ON_PROGRESS
        }

        return QuranItemStatus.NONE

    }
}