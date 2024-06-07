package com.simsinfotekno.maghribmengaji.ui.audioplayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simsinfotekno.maghribmengaji.model.QuranRecordingStudent
import com.simsinfotekno.maghribmengaji.usecase.UploadRecordingStudentUseCase
import kotlinx.coroutines.launch

class AudioPlayerViewModel: ViewModel() {
    private val uploadRecordingStudentUseCase = UploadRecordingStudentUseCase()
    private val _selectedAudioUri = MutableLiveData<Uri?>()
    val selectedAudioUri: LiveData<Uri?> get() = _selectedAudioUri

    private val _uploadResult = MutableLiveData<Result<Unit>>()
    val uploadResult: LiveData<Result<Unit>> get() = _uploadResult

    fun setSelectedAudioUri(uri: Uri) {
        _selectedAudioUri.value = uri
    }

    fun uploadAudioRecord(audioRecord: QuranRecordingStudent) {
        viewModelScope.launch {
            uploadRecordingStudentUseCase(
                audioRecord,
                onSuccess = {
                    _uploadResult.value = Result.success(Unit)
                },
                onFailure = { exception ->
                    _uploadResult.value = Result.failure(exception)
                }
            )
        }
    }
}
