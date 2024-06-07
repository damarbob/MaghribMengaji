package com.simsinfotekno.maghribmengaji.usecase

import android.net.Uri
import com.simsinfotekno.maghribmengaji.model.QuranRecordingStudent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranRecordingStudentRepository

class UploadRecordingStudentUseCase {
    suspend operator fun invoke(
        audioRecord: QuranRecordingStudent,
        onSuccess: (Unit) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val storageReference =
                FirebaseStorage.getInstance().reference.child("audio/${audioRecord.studentId}/${audioRecord.studentId}-${audioRecord.pageId}")
            val uploadTask = storageReference.putFile(Uri.parse(audioRecord.recordingUriString)).await()

            if (uploadTask.task.isSuccessful) {
                val downloadUrl = storageReference.downloadUrl.await().toString()
                val updatedAudioRecord = audioRecord.copy(recordingUriString = downloadUrl)

                FirebaseFirestore.getInstance()
                    .collection(QuranRecordingStudent.COLLECTION)
                    .document("${audioRecord.studentId}-${audioRecord.pageId}")
                    .set(updatedAudioRecord)
                    .await()

                quranRecordingStudentRepository.createRecord(updatedAudioRecord)
                onSuccess(Unit)
            } else {
                onFailure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
