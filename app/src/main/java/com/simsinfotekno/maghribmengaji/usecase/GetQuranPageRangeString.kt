package com.simsinfotekno.maghribmengaji.usecase

import androidx.annotation.IntegerRes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository

class GetQuranPageRangeString {
    // Function to get the page range string for a volume
    operator fun invoke(volumeId: Int, @IntegerRes pageString: String): String {
        val volume = quranVolumeRepository.getRecordById(volumeId)
        val firstPage = volume?.pageIds?.first()
        val lastPage = volume?.pageIds?.last()
        return "$pageString $firstPage-$lastPage"
    }
}