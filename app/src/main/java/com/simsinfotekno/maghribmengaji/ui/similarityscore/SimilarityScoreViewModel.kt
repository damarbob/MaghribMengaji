package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.simsinfotekno.maghribmengaji.usecase.*
import kotlinx.coroutines.launch

class SimilarityScoreViewModel : ViewModel(), FetchQuranPageUseCase.ResultHandler,
    OCRAsyncTask.IOCRCallBack {
    private val _similarityScore = MutableLiveData<Int>()
    val similarityScore: LiveData<Int> get() = _similarityScore

    private val _progressVisibility = MutableLiveData<Boolean>()
    val progressVisibility: LiveData<Boolean> get() = _progressVisibility

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val ocrAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase(this)
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()

    private lateinit var quranApiResult: String
    private lateinit var ocrResult: String

    fun fetchQuranPage(pageId: Int) {
        fetchQuranPageTask(pageId)
    }

    fun startOCRProcessing(imageBase64: String, progressBar: CircularProgressIndicator) {
        _progressVisibility.value = true
        viewModelScope.launch {
            ocrAsyncTask.execute(imageBase64, "ara", this@SimilarityScoreViewModel)
        }
    }

    override fun onSuccess(result: String) {
        quranApiResult = extractTextFromQuranApiJson(result)
    }

    override fun onFailure(message: String) {
        _message.value = message
        _progressVisibility.value = false
    }

    override fun getOCRCallBackResult(response: String?) {
        ocrResult = extractTextFromOCRApiJson(response!!).toString()
        val similarityIndex = jaccardSimilarityIndex(quranApiResult, ocrResult)
        _similarityScore.value = similarityIndex.toInt()
        _progressVisibility.value = false
    }

    override fun onOCRFailure(exception: Exception) {
        _message.value = exception.message
        _progressVisibility.value = false
    }
}