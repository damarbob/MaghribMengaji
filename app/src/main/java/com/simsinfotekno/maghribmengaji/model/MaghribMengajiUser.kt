package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class MaghribMengajiUser(
    var id: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var address: String? = null, // For student
    var school: String? = null, // For student
    var role: String? = ROLE_STUDENT,
    var lastPageId: Int? = null,
    var ustadhId: String? = null,
    var referralCode: String? = null, // For student and influencer
    var balance: Int? = null, // For teacher and influencer
    var bank: String? = null, // For teacher and influencer
    var bankAccount: Int? = null, // For teacher and influencer
    var ownedVolumeId: List<Int>? = null, // Owned volume. Either by previlege or purchase.
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) {
    companion object {
        val COLLECTION = "users"
        val ROLE_STUDENT = "student"
        val ROLE_TEACHER = "teacher"
        val ROLE_INFLUENCER = "influencer"
        val ROLE_ADMIN = "admin"
    }
}