package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simsinfotekno.maghribmengaji.usecase.UpdateStudentScoreUseCase
import kotlinx.coroutines.launch

class UstadhScoringViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhPageStudentFragment::class.java.simpleName
    }

    private val updateStudentScoreUseCase = UpdateStudentScoreUseCase()
    private val _updateStudentScoreResult = MutableLiveData<Result<String>?>(null)
    val updateStudentScoreResult: MutableLiveData<Result<String>?> get() = _updateStudentScoreResult

    fun resetLiveData()
    {
        _updateStudentScoreResult.value = null
    }
    fun updateStudentScore(
        studentUserId: String,
        pageId: Int,
        tidinessScore: Int,
        accuracyScore: Int,
        consistencyScore: Int
    ) {
        viewModelScope.launch {
            updateStudentScoreUseCase(
                studentUserId = studentUserId,
                pageId = pageId,
                tidinessScore = tidinessScore,
                accuracyScore = accuracyScore,
                consistencyScore = consistencyScore,
                onSuccess = { studentId ->
                    _updateStudentScoreResult.value = Result.success(studentId)
                },
                onFailure = { exception ->
                    _updateStudentScoreResult.value = Result.failure(exception)
                }
            )
        }
    }

}