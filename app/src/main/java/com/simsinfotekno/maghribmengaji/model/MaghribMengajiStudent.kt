package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiStudent(
    var id: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var lastPageId: Int? = null,
    var teacherId: String? = null,
) {
    companion object {
        val COLLECTION = "students"
    }
}