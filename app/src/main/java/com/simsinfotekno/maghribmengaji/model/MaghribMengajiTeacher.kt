package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiTeacher(
    var id: String,
    var fullName: String,
    var email: String,
    var phone: String,
    var studentIds: List<Int>? = null,
)
