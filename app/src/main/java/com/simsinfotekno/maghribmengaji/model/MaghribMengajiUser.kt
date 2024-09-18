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
    var bankAccount: String? = null, // For teacher and influencer
    var ownedVolumeId: List<Int>? = null, // Owned volume. Either by privilege or purchase. TODO: Create a new model
    var lastDailySubmit: Timestamp? = null,
    var currentSubmitStreak: Int? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) {
    companion object {
        val COLLECTION = "users"
        val ROLE_STUDENT = "student"
        val ROLE_TEACHER = "teacher"
        val ROLE_AFFILIATE = "affiliate"
        val ROLE_ADMIN = "admin"
        val TEACHER_DEPOSIT = 25000 // Initial deposit to replace the application cost
        val TEACHER_REWARD = 5000 // IDR 5000 is given to teacher for each new student choosing him
        val AFFILIATE_REWARD = 5000 // IDR 5000 is given to affiliate for each new student referring him
    }
}