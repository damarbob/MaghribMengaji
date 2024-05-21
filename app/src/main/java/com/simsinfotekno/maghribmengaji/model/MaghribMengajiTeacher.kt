package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiTeacher(
    var fullName: String,
    var email: String,
    var phone: String,
    var studentIds: List<Int>? = null,
)
