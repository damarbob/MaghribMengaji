package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.event.OnPageStudentRepositoryUpdate
import com.simsinfotekno.maghribmengaji.event.OnRepositoryUpdate
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.EditDistanceSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.ExtractQRCodeToPageIdUseCase
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromOCRApiJSON
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromQuranAPIJSON
import com.simsinfotekno.maghribmengaji.usecase.FetchQuranPageUseCase
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.JaroSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.JaroWinklerSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.MaghribBonusUseCase
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import com.simsinfotekno.maghribmengaji.usecase.QRCodeScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.RabinKarpSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.SubmitStreakBonusUseCase
import com.simsinfotekno.maghribmengaji.usecase.UpdateLastPageId
import com.simsinfotekno.maghribmengaji.usecase.UpdateSubmitStreakUseCase
import com.simsinfotekno.maghribmengaji.usecase.UploadFileToFirebaseStorageUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SimilarityScoreViewModel : ViewModel() {

    companion object {
        private val TAG = SimilarityScoreViewModel::class.java.simpleName
        private const val TIMEOUT_DURATION = 30000L // 30 seconds
    }

//    var pageId: Int? = null
    var bitmap: Bitmap? = null
    var imageUriString: String? = null

    /* Live data */
    private val _pageId = MutableLiveData<Int?>()
    val pageId: LiveData<Int?> get() = _pageId

    private val _remoteDbResult = MutableLiveData<Result<QuranPageStudent>?>()
    val remoteDbResult: LiveData<Result<QuranPageStudent>?> get() = _remoteDbResult

    private val _progressVisibility = MutableLiveData<Boolean>()
    val progressVisibility: LiveData<Boolean> get() = _progressVisibility

    private val _similarityScore = MutableLiveData<Int?>(null)
    val similarityScore: LiveData<Int?> get() = _similarityScore
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    private val _maghribBonus = MutableLiveData(0)
    val maghribBonus: LiveData<Int> get() = _maghribBonus
    private val _submitStreakBonus = MutableLiveData<List<Float>>()
    val submitStreakBonus: LiveData<List<Float>> get() = _submitStreakBonus
    private val _totalScore = MutableLiveData<Int>()
    val totalScore: LiveData<Int> get() = _totalScore

    private val quranApiResultDeferred = CompletableDeferred<String>()
    private var ocrResult: String? = null
    private var quranAPIResult: String? = null

    /* Use cases */
    private val uploadFileToFirebaseStorageUseCase = UploadFileToFirebaseStorageUseCase()
    private val updateLastPageId = UpdateLastPageId()
    private val ocrAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()
    private val bitmapToBase64 = BitmapToBase64()
    private val updateSubmitStreakUseCase = UpdateSubmitStreakUseCase()
    private val qrCodeScannerUseCase = QRCodeScannerUseCase()
    private val extractQRCodeToPageIdUseCase = ExtractQRCodeToPageIdUseCase()
    private val maghribBonusUseCase = MaghribBonusUseCase()
    private val submitStreakBonusUseCase = SubmitStreakBonusUseCase()
    /*Algorithm*/
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val editDistanceSimilarityIndex= EditDistanceSimilarityIndex()
    private val jaroWinklerSimilarityIndex= JaroWinklerSimilarityIndex()
    private val jaroSimilarityIndex= JaroSimilarityIndex()
    private val rabinKarpSimilarityIndex= RabinKarpSimilarityIndex()

    fun setPageId(pageId: Int?){
        _pageId.value = pageId
    }

    fun getPageId():Int? {
        return _pageId.value
    }

    fun checkQRCode(onSuccess: () -> Unit, onError: (Any) -> Unit) {
        Log.d(TAG, "QR code scanning...")
        qrCodeScannerUseCase(bitmap!!, onBarcodeSuccess = { result ->
            Log.d(TAG, "QR code success...")
            if (result != null) {
                val pageId = extractQRCodeToPageIdUseCase(result)

                if (pageId != null && this.pageId.value == pageId) onSuccess() // If pageId is found, call onSuccess
                else if (pageId != null && pageId != this.pageId.value) {
                    onError(pageId)
                    _progressVisibility.value = false
                } else {
                    onError(ExtractQRCodeToPageIdUseCase.PAGE_ID_NOT_FOUND) // If pageId is not found or not match, call onError
                    _progressVisibility.value = false
                }
            } else {
                onError(QRCodeScannerUseCase.QR_CODE_NOT_FOUND) // If QR Code not found, call onError
                _progressVisibility.value = false
            }
        }, onBarcodeError = {
            Log.d(TAG, "QR code error...")
            it.localizedMessage?.let { it1 -> onError(it1) } // If there is an error, call onError with the error message
            _progressVisibility.value = false
        })
    }

    // Function to initiate OCR processing
    fun processOCR(
        language: String,
        lifecycleCoroutineScope: LifecycleCoroutineScope
    ) {
        _progressVisibility.value = true
        callQuranApi()
        applyImageFilter2 { bitmap ->
            ocrAsyncTask(
                bitmapToBase64(bitmap),
                language,
                lifecycleCoroutineScope,
                onOCRCallbackResult = { response ->
                    viewModelScope.launch {
                        ocrResult = withContext(Dispatchers.IO) {
                            response?.let {
                                extractTextFromOCRApiJson(it)
                            }.toString()
                        }

                        // Wait until quranApiResult is ready
                        quranAPIResult = quranApiResultDeferred.await()

                        // Ensure quranApiResult is ready before calculating similarity index
                        calculateSimilarityIndex()
                    }
                },
                onOCRFailure = {
                    _errorMessage.value = it.localizedMessage
                    _progressVisibility.value = false
                })
        }
    }

    // Placeholder for Quran API call
    private fun callQuranApi() {
        _progressVisibility.value = true
        fetchQuranPageTask(
            pageId.value!!,
            onSuccess = { result ->
                // Handle success, e.g., update UI
                viewModelScope.launch {
                    val extractedQuranResult = withContext(Dispatchers.IO) {
                        extractTextFromQuranApiJson(result)
                    }
                    quranApiResultDeferred.complete(extractedQuranResult)

                    // If ocrResult is already ready, calculate similarity index
                    calculateSimilarityIndex()

                }
            }, onFailure = { exception ->
                _errorMessage.value = exception
                _progressVisibility.value = false
            })
//        viewModelScope.launch(Dispatchers.IO) {
//            // Implement Quran API call logic
//            val result = "Quran API response here" // Replace with actual API call
//            withContext(Dispatchers.Main) {
//                quranResult.value = result
//            }
//        }
    }

    private suspend fun calculateSimilarityIndex() {
        _progressVisibility.value = true
        withContext(Dispatchers.Main) {
            _similarityScore.value = ocrResult?.let {
                quranAPIResult?.let { it1 ->
                    jaccardSimilarityIndex(it, it1).toInt()
                //Replace with actual similarity index calculation logic based on your algorithm
                }
            }

            // Check if all required data is available before calling total()
            if (_similarityScore.value != null && maghribBonus.value != null && submitStreakBonus.value != null) {
                totalScore()
            }
        }
    }

    private suspend fun totalScore() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Ensure the necessary data is not null
                val similarityScoreValue = similarityScore.value ?: 0
                val maghribBonusValue = maghribBonus.value ?: 0
                val submitStreakMultiplier = submitStreakBonus.value?.getOrNull(1) ?: 1f

                // Calculate the total score
                _totalScore.postValue(
                    if (((similarityScoreValue + maghribBonusValue).toFloat() * submitStreakMultiplier).toInt() <= 100) ((similarityScoreValue + maghribBonusValue).toFloat() * submitStreakMultiplier).toInt() else 100
                )
                Log.d(TAG, "Total score: ${_totalScore.value}")
            }
        }
    }

    private fun applyImageFilter2(documentFilterCallback: DocumentFilter.CallBack<Bitmap>) {
        val documentFilter = DocumentFilter()
        documentFilter.getGreyScaleFilter(bitmap, documentFilterCallback)
    }

    init {
        EventBus.getDefault().register(this)
        _maghribBonus.postValue(maghribBonusUseCase())
        _submitStreakBonus.postValue(submitStreakBonusUseCase())
    }

    override fun onCleared() {
        super.onCleared()

        EventBus.getDefault().unregister(this)
    }

    //    fun uploadPageStudent() {
//        _progressVisibility.value = true
//
//        // Only add record if page student with specified id is not found, if not, update the picture uri instead
//        val pageStudent = quranPageStudentRepository.getRecordByPageId(pageId)
//
//        if (pageStudent != null) {
//            pageStudent.pictureUriString = imageUriString
//            _progressVisibility.value = false
//        } else {
//
//            quranPageStudentRepository.addRecord(
//                QuranPageStudent(
//                    pageId!!,
//                    studentRepository.getStudent().id!!,
//                    studentRepository.getStudent().ustadhId,
//                    pictureUriString = imageUriString,
//                    createdAt = Timestamp.now(),
//                )
//            )
//
//            // Update last page id in the db
//            updateLastPageId(pageId!!, { Log.d(TAG, "Successfully updated last page id") }) {
//                Log.e(TAG, "Failed to update last page id: $it")
//            }
//        }
//    }
    fun updateSubmitStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            updateSubmitStreakUseCase { result ->
                result.onSuccess {
                    Log.d(TAG, "Successfully updated submit streak")
                }
                result.onFailure {
                    Log.e(TAG, "Failed to update submit streak: $it")
                }
            }
        }
    }

    fun uploadPageStudent() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeout(TIMEOUT_DURATION) {
                    _progressVisibility.postValue(true)

                    // Only add record if page student with specified id is not found, if not, update the picture uri instead
                    val pageStudent = quranPageStudentRepository.getRecordByPageId(pageId.value)

                    if (pageStudent != null) {
                        pageStudent.pictureUriString = imageUriString
                        _progressVisibility.postValue(false)
                    } else {
                        quranPageStudentRepository.addRecord(
                            QuranPageStudent(
                                pageId.value,
                                studentRepository.getStudent()?.id,
                                studentRepository.getStudent()?.ustadhId,
                                pictureUriString = imageUriString,
                                oCRScore = totalScore.value?.toInt(),
                                createdAt = Timestamp.now(),
                            )
                        )

                        // Update last page id in the db
                        updateLastPageId(
                            pageId.value!!,
                            { Log.d(TAG, "Successfully updated last page id") }) {
                            Log.e(TAG, "Failed to update last page id: $it")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Timeout or error during uploadPageStudent: $e")
                _progressVisibility.postValue(false)
                _remoteDbResult.postValue(Result.failure(e))
            }
        }
    }

    // Event listeners
    // Subscribe to OnPageStudentRepositoryUpdate event
//    @Subscribe(threadMode = ThreadMode.POSTING)
//    fun _083305312024(event: OnPageStudentRepositoryUpdate) {
//        if (event.status == OnRepositoryUpdate.Event.ACTION_ADD) {
//
//            val remoteDb = Firebase.firestore.collection(QuranPageStudent.COLLECTION)
//
//            val record = event.pageStudent ?: return
//            record.oCRScore = oCRScore?.toInt()
//
//            remoteDb
//                .whereEqualTo("studentId", record.studentId)
//                .whereEqualTo("pageId", record.pageId)
//                .get()
//                .addOnCompleteListener { task ->
//
//                    if (task.isSuccessful) {
//
//                        if (task.result.isEmpty) {
//
//                            // No documents found
//                            Log.d(TAG, "No matching documents found. Proceeding...")
//
//                            /* Upload use case */
//                            uploadFileToFirebaseStorageUseCase.invoke(
//                                Uri.parse(record.pictureUriString),
//                                record.pageId.toString(),
//                                "${QuranPageStudent.COLLECTION}/${record.studentId}",
//                                { url ->
//
//                                    // If upload succeeded, update the pictureUriString inside record to the remote url
//                                    record.pictureUriString = url
//
//                                    // Add the record to remote database
//                                    remoteDb.add(record).addOnCompleteListener {
//                                        _progressVisibility.value = false
//                                        if (it.isSuccessful) {
//                                            _remoteDbResult.value =
//                                                Result.success(record) // Return user
//                                        } else {
//                                            _remoteDbResult.value = Result.failure(
//                                                it.exception
//                                                    ?: Exception("Failed to upload QuranPageStudent data to remote database")
//                                            )
//                                        }
//                                    }
//                                },
//                                {
//                                    _progressVisibility.value = false
//                                    _remoteDbResult.value = Result.failure(it)
//                                }
//                            )
//
//                        } else {
//                            _progressVisibility.value = false
//
//                            for (document in task.result) {
//                                // Process the document data here
//                                val data = document.data
//                                // For example, you can log the document ID and data
//                                Log.d(TAG, "Document with the same pageId already exists!")
//                                _remoteDbResult.value =
//                                    Result.failure(Exception("Document with the same pageId already exists!"))
//                            }
//
//                        }
//
//                    } else {
//                        Log.w(TAG, "Error getting documents.", task.exception)
//                        _remoteDbResult.value =
//                            Result.failure(Exception("Error getting documents. ${task.exception}"))
//                        _progressVisibility.value = false
//                    }
//                }
//
//        }
//    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun _083305312024(event: OnPageStudentRepositoryUpdate) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeout(TIMEOUT_DURATION) {
                    if (event.status == OnRepositoryUpdate.Event.ACTION_ADD) {
                        val remoteDb = Firebase.firestore.collection(QuranPageStudent.COLLECTION)

                        val record = event.pageStudent ?: return@withTimeout
//                        record.oCRScore = similarityScore.value?.toInt()

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
                                                remoteDb.add(record).addOnCompleteListener {
                                                    _progressVisibility.postValue(false)
                                                    if (it.isSuccessful) {
                                                        _remoteDbResult.postValue(
                                                            Result.success(record) // Return user
                                                        )
//                                                        updateSubmitStreakUseCase(context)
                                                    } else {
                                                        _remoteDbResult.postValue(
                                                            Result.failure(
                                                                it.exception
                                                                    ?: Exception("Failed to upload QuranPageStudent data to remote database")
                                                            )
                                                        )
                                                    }
                                                }
                                            },
                                            {
                                                _progressVisibility.postValue(false)
                                                _remoteDbResult.postValue(Result.failure(it))
                                            }
                                        )

                                    } else {
                                        _progressVisibility.postValue(false)

                                        for (document in task.result) {
                                            // Process the document data here
                                            val data = document.data
                                            // For example, you can log the document ID and data
                                            Log.d(
                                                TAG,
                                                "Document with the same pageId already exists!"
                                            )
                                            _remoteDbResult.postValue(
                                                Result.failure(Exception("Document with the same pageId already exists!"))
                                            )
                                        }

                                    }

                                } else {
                                    Log.w(TAG, "Error getting documents.", task.exception)
                                    _remoteDbResult.postValue(
                                        Result.failure(Exception("Error getting documents. ${task.exception}"))
                                    )
                                    _progressVisibility.postValue(false)
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Timeout or error during event handling: $e")
                _progressVisibility.postValue(false)
                _remoteDbResult.postValue(Result.failure(e))
            }
        }
    }


    val similarityIndex = MutableLiveData<Int>()
}