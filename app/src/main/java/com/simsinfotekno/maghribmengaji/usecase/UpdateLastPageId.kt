package com.simsinfotekno.maghribmengaji.usecase

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class UpdateLastPageId {
    operator fun invoke(
        pageId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = Firebase.auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        db.collection(MaghribMengajiUser.COLLECTION).whereEqualTo("id", uid).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val documentId = document.id

                    val lastPageInfo = hashMapOf(
                        "lastPageId" to pageId,
                        "updatedAt" to Timestamp.now()
                    )

                    db.collection(MaghribMengajiUser.COLLECTION).document(documentId)
                        .update(lastPageInfo as Map<String, Any>)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            db.collection(MaghribMengajiUser.COLLECTION).document(documentId)
                                .set(lastPageInfo)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { setException ->
                                    onFailure(setException)
                                }
                        }

                } else {
                    onFailure(Exception("No user found with id = $uid"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}