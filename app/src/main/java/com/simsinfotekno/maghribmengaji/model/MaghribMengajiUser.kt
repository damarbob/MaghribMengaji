package com.simsinfotekno.maghribmengaji.model

data class MaghribMengajiUser(
    var id: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var role: String? = ROLE_STUDENT,
    var lastPageId: Int? = null,
    var ustadhId: String? = null,
) {
    companion object {
        val COLLECTION = "users"
        val ROLE_STUDENT = "student"
        val ROLE_TEACHER = "teacher"
    }
}