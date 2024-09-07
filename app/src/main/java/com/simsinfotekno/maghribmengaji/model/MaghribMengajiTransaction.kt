package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class MaghribMengajiTransaction (
    val userId: String? = null,
    val type: String? = null,
    val amount: Int? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) {
    companion object {
        val TYPE_DEPOSIT = "deposit"
        val TYPE_WITHDRAWAL = "withdrawal"
        val TYPE_PAYMENT = "payment"
    }
}