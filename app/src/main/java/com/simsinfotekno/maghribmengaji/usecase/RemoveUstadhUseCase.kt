package com.simsinfotekno.maghribmengaji.usecase

import com.google.firebase.firestore.FirebaseFirestore
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class RemoveUstadhUseCase {

    companion object {
        private val TAG = RemoveUstadhUseCase::class.simpleName
    }

    // Set the ustadhId from user to null
    operator fun invoke(userId: String, onResult: (Boolean, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Update the user document
        db.collection(MaghribMengajiUser.COLLECTION).whereEqualTo("id", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val documentId = document.id

                    val lastPageInfo = hashMapOf(
                        "ustadhId" to null
                    )

                    db.collection(MaghribMengajiUser.COLLECTION).document(documentId)
                        .update(lastPageInfo as Map<String, Any>)
                        .addOnSuccessListener {
                            onResult(true, null) // Success
                        }
                        .addOnFailureListener { exception ->
                            db.collection(MaghribMengajiUser.COLLECTION).document(documentId)
                                .set(lastPageInfo)
                                .addOnSuccessListener {
                                    onResult(true, null) // Success
                                }
                                .addOnFailureListener { setException ->
                                    onResult(false, setException.localizedMessage) // Failure
                                }
                        }

                } else {
                    onResult(false, "No user found with id = $userId")
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.localizedMessage) // Failure
            }

    }
}