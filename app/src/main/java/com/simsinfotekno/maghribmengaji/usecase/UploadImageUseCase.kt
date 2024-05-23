package com.simsinfotekno.maghribmengaji.usecase

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadImageUseCase {

    private var pageId: Int = -1

    /*
    UPLOAD IMAGE TO FIRESTORE DOCUMENT
     */

    // Find document based on quranPages id then upload image to it TODO: Move to use case
    operator fun invoke(
        idValue: Int,
        fileUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        pageId = idValue

        val db = FirebaseFirestore.getInstance()
        db.collection("quranPages").whereEqualTo("id", idValue).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val documentId = document.id
                    uploadAndSaveImageWithId(fileUri, documentId, onSuccess, onFailure)
                } else {
                    onFailure(Exception("No document found with id = $idValue"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    private fun uploadAndSaveImageWithId(
        fileUri: Uri,
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        uploadImageWithId(fileUri, { imageUrl ->
            saveImageUrlToFirestoreWithId(imageUrl, documentId, onSuccess, onFailure)
        }, onFailure)
    }

    private fun uploadImageWithId(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference
        val imageRef: StorageReference = storageRef.child("quranPages/$pageId")

        val uploadTask = imageRef.putFile(fileUri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    private fun saveImageUrlToFirestoreWithId(
        imageUrl: String,
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val imageInfo = hashMapOf(
            "picture" to imageUrl,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("quranPages").document(documentId)
            .update(imageInfo as Map<String, Any>)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                db.collection("quranPages").document(documentId)
                    .set(imageInfo)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { setException ->
                        onFailure(setException)
                    }
            }
    }

    /*
    END OF UPLOAD IMAGE TO FIRESTORE DOCUMENT
     */
}