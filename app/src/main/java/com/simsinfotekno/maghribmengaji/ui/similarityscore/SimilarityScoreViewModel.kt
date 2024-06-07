package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.event.OnPageStudentRepositoryUpdate
import com.simsinfotekno.maghribmengaji.event.OnRepositoryUpdate
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.UploadFileToFirebaseStorageUseCase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SimilarityScoreViewModel : ViewModel() {

    companion object {
        private val TAG = SimilarityScoreViewModel::class.java.simpleName
    }

    var pageId: Int? = null
    var bitmap: Bitmap? = null
    var imageUriString: String? = null
    var oCRScore: Double? = null // Score for upload

    /* Live data */
    private val _remoteDbResult = MutableLiveData<Result<QuranPageStudent>?>()
    val remoteDbResult: LiveData<Result<QuranPageStudent>?> get() = _remoteDbResult

    private val _progressVisibility = MutableLiveData<Boolean>()
    val progressVisibility: LiveData<Boolean> get() = _progressVisibility

    /* Use cases */
    private val uploadFileToFirebaseStorageUseCase = UploadFileToFirebaseStorageUseCase()

    init {
        EventBus.getDefault().register(this)
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    fun uploadPageStudent() {
        _progressVisibility.value = true

        // Only add record if page student with specified id is not found, if not, update the picture uri instead
        val pageStudent = quranPageStudentRepository.getRecordByPageId(pageId)

        if (pageStudent != null) {
            pageStudent.pictureUriString = imageUriString
            _progressVisibility.value = false
        }
        else {
            quranPageStudentRepository.addRecord(
                QuranPageStudent(
                    pageId!!,
                    studentRepository.getStudent().id!!,
                    studentRepository.getStudent().ustadhId,
                    pictureUriString = imageUriString,
                    createdAt = Timestamp.now(),
                )
            )
        }

    }

    // Event listeners
    // Subscribe to OnSelectedPlaceChangedEvent event
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun _083305312024(event: OnPageStudentRepositoryUpdate) {
        if (event.status == OnRepositoryUpdate.Event.ACTION_ADD) {

            val remoteDb = Firebase.firestore.collection(QuranPageStudent.COLLECTION)

            val record = event.pageStudent ?: return
            record.oCRScore = oCRScore?.toInt()

            remoteDb
                .whereEqualTo("studentId", record.studentId)
                .whereEqualTo("pageId", record.pageId)
                .get()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        if (task.result.isEmpty) {

                            // No documents found
                            Log.d(TAG, "No matching documents found. Proceeding...")

                            /* Upload use case */
                            uploadFileToFirebaseStorageUseCase.invoke(
                                Uri.parse(record.pictureUriString),
                                record.pageId.toString(),
                                "${QuranPageStudent.COLLECTION}/${record.studentId}",
                                { url ->

                                    // If upload succeeded, update the pictureUriString inside record to the remote url
                                    record.pictureUriString = url

                                    // Add the record to remote database
                                    remoteDb.add(record).addOnCompleteListener{
                                        _progressVisibility.value = false
                                        if (it.isSuccessful) {
                                            _remoteDbResult.value = Result.success(record) // Return user
                                        }
                                        else {
                                            _remoteDbResult.value = Result.failure(it.exception ?: Exception("Failed to upload QuranPageStudent data to remote database"))
                                        }
                                    }
                                },
                                {
                                    _progressVisibility.value = false
                                    _remoteDbResult.value = Result.failure(it)
                                }
                            )

                        } else {
                            _progressVisibility.value = false

                            for (document in task.result) {
                                // Process the document data here
                                val data = document.data
                                // For example, you can log the document ID and data
                                Log.d(TAG, "Document with the same pageId already exists!")
                                _remoteDbResult.value =
                                    Result.failure(Exception("Document with the same pageId already exists!"))
                            }

                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                        _remoteDbResult.value = Result.failure(Exception("Error getting documents. ${task.exception}"))
                        _progressVisibility.value = false
                    }
                }

        }
    }


    val similarityIndex = MutableLiveData<Int>()
}