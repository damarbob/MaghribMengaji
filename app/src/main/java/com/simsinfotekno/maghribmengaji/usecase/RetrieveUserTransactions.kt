package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiTransaction

class RetrieveUserTransactions {

    companion object {
        private val TAG = RetrieveUserTransactions::class.simpleName
    }

    // Method to retrieve the user profile
    operator fun invoke(userId: String, onResult: (Result<List<MaghribMengajiTransaction>>) -> Unit) {
        val db = Firebase.firestore.collection(MaghribMengajiTransaction.COLLECTION)

        db
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = arrayListOf<MaghribMengajiTransaction>()

                Log.d(TAG, "Transactions found for userId: $userId")

                for (document in documents) {
                    val transactionData = document.data

                    Log.d(TAG, "Transaction found with ID: ${document.id} => $transactionData")

                    val transaction = MaghribMengajiTransaction(
                        document.id,
                        (transactionData["userId"] as String),
                        (transactionData["type"] as String),
                        (transactionData["amount"] as? Long)?.toInt(),
                        (transactionData["balance"] as? Long)?.toInt(),
                        (transactionData["goods"] as? List<String>?),
                        (transactionData["approvedAt"] as? Timestamp),
                        (transactionData["createdAt"] as? Timestamp),
                        (transactionData["updatedAt"] as? Timestamp),
                    )

                    transactions.add(transaction)
                }

                // Call the lambda function with the list of page students
                onResult(Result.success(transactions))

                if (documents.isEmpty) {
                    Log.d(TAG, "No matching transactions found.")
                }
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
                Log.e(TAG, "Error getting documents: $e")
            }
    }
}