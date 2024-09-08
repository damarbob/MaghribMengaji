package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiTransaction
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class TransactionService {

    companion object {
        val TAG = TransactionService::class.simpleName
    }

    /* Example usage
    transactionService.deposit(user, amount) { result ->
        result.onSuccess {
            // Handle success
        }.onFailure { exception ->
            // Handle failure
        }
    }*/

    fun deposit(amount: Int, onResult: (Result<MaghribMengajiTransaction>) -> Unit) {
        // Async logic here
        // Example usage:
        // callback(Result.success(Unit)) // for success
        // callback(Result.failure(Exception("Error message"))) // for failure

        Log.d(TAG, "Starting deposit")

        // Async logic here
        val db = FirebaseFirestore.getInstance().collection(MaghribMengajiTransaction.COLLECTION)
        val user = MainApplication.studentRepository.getStudent() // Get current user
        val balance = MainApplication.transactionRepository.getBalance() // Get current user balance

        val transaction = MaghribMengajiTransaction(
            userId = user?.id,
            type = MaghribMengajiTransaction.TYPE_DEPOSIT,
            amount = amount, // Amount withdrawn
            balance = (balance ?: 0).plus(amount), // Decrease balance depending on the withdrawn amount
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
        )

        db.add(transaction)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                transaction.id = documentId // Set transaction id to match in the database

                MainApplication.transactionRepository.getRecords().add(0, transaction) // Add record in repository

                onResult(Result.success(transaction))

                Log.d(TAG, "Transaction ${transaction.type} added with ID: $documentId")
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
                Log.d(TAG, "Error adding transaction: ${e.message}")
            }
    }

    fun withdraw(amount: Int, onResult: (Result<MaghribMengajiTransaction>) -> Unit) {
        Log.d(TAG, "Starting withdraw")

        // Async logic here
        val db = FirebaseFirestore.getInstance().collection(MaghribMengajiTransaction.COLLECTION)
        val user = MainApplication.studentRepository.getStudent() // Get current user
        val balance = MainApplication.transactionRepository.getBalance() // Get current user balance

        val transaction = MaghribMengajiTransaction(
            userId = user?.id,
            type = MaghribMengajiTransaction.TYPE_WITHDRAWAL,
            amount = amount, // Amount withdrawn
            balance = (balance ?: 0).minus(amount), // Decrease balance depending on the withdrawn amount
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
        )

        db.add(transaction)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                transaction.id = documentId // Set transaction id to match in the database

                MainApplication.transactionRepository.getRecords().add(0, transaction) // Add record in repository

                onResult(Result.success(transaction))

                Log.d(TAG, "Transaction ${transaction.type} added with ID: $documentId")
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
                Log.d(TAG, "Error adding transaction: ${e.message}")
            }
    }

    fun makePayment(payer: MaghribMengajiUser, payee: MaghribMengajiUser, amount: Int, callback: (Result<Unit>) -> Unit) {
        // Async logic here
    }

}