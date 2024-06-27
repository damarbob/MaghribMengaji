package com.simsinfotekno.maghribmengaji.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeByStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeViewModel() : ViewModel() {

    private val _volumeInProgressDataSet = MutableLiveData<List<QuranVolume>>().apply {
        value = null
    }
    val volumeInProgressDataSet: LiveData<List<QuranVolume>> = _volumeInProgressDataSet

    private val _lastPageId = MutableLiveData<Int>().apply { value = null }
    val lastPageId = _lastPageId

    private val _progressVisibility = MutableLiveData<Boolean>(true)
    val progressVisibility: LiveData<Boolean> get() = _progressVisibility

    /* Use case */
    private val getQuranVolumeByStatus = GetQuranVolumeByStatus()

    init {
        EventBus.getDefault().register(this)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
        _progressVisibility.value = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun _105606032024(event: OnUserDataLoaded) {
        if (event.userDataEvent == UserDataEvent.PAGE) {
            _volumeInProgressDataSet.value = getQuranVolumeByStatus.invoke(QuranItemStatus.ON_PROGRESS)

            _lastPageId.value = studentRepository.getStudent().lastPageId
            _progressVisibility.value = false
        }
    }

}