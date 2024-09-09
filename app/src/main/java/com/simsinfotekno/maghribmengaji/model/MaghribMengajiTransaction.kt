package com.simsinfotekno.maghribmengaji.model

import com.google.firebase.Timestamp

data class MaghribMengajiTransaction (
    var id: String? = null,
    var userId: String? = null,
    var type: String? = null,
    var amount: Int? = null,
    var balance: Int? = null,
    var goods: List<String>? = null,
    var approvedAt: Timestamp? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
) {
    companion object {
        val COLLECTION = "transactions"
        val TYPE_DEPOSIT = "deposit"
        val TYPE_WITHDRAWAL = "withdrawal"
        val TYPE_PAYMENT = "payment"
    }
}