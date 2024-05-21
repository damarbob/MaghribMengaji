package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiStudent(
    var fullName: String,
    var email: String,
    var lastPageId: Int? = null,
    var finishedPageIds: List<Int>? = null, // TODO: Delete soon
    var onProgressPageIds: List<Int>? = null, // TODO: Delete soon
    var achievementIds: List<Int>? = null, // Achievement concept
    var teacherId: Int? = null,
)
