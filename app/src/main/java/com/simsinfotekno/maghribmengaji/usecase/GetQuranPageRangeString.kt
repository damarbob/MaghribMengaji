package com.simsinfotekno.maghribmengaji.usecase

class GetQuranPageRangeString {
    // Function to get the page range string for a volume
    operator fun invoke(pageIds: List<Int>?, pageString: String): String {
        val firstPage = pageIds?.first()
        val lastPage = pageIds?.last()
        return if (pageIds != null) "$pageString $firstPage-$lastPage" else pageString
    }
}