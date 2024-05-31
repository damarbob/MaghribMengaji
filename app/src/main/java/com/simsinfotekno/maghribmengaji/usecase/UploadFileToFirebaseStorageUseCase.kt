package com.simsinfotekno.maghribmengaji.usecase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadFileToFirebaseStorageUseCase {

    /**
     * Upload file to firebase storage
     * @param fileUri Uri of the file to be uploaded
     * @param fileName file name to be set on the Firebase storage
     * @param collection collection name or collection path in the Firebase Storage
     * @param onSuccess On success callback
     * @param onFailure On failure callback
     * @throws Exception
      */
    operator fun invoke(
        fileUri: Uri,
        fileName: String,
        collection: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference
        val imageRef: StorageReference = storageRef.child("$collection/$fileName")

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

}