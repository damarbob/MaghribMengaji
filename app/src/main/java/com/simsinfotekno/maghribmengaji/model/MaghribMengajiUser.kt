package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiUser(
    var fullName: String,
    var email: String,
    var lastPageId: Int? = null,
    var finishedPageIds: List<Int>? = null,
    var onProgressPageIds: List<Int>? = null,
    var achievementIds: List<Int>? = null, // Achievement concept
)
