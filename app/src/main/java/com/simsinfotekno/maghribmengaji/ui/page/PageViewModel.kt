package com.simsinfotekno.maghribmengaji.ui.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {
    private var volumeId: Int = 0
    var pageId: Int? = 0

    // Set page view mode:
    // True -> page view
    // False -> result view
    private val _pageViewMode = MutableLiveData<Boolean>()
    val pageViewMode: LiveData<Boolean> get() = _pageViewMode

    fun setToPageViewMode() {
        _pageViewMode.value = true
    }
    fun setToResultViewMode() {
        _pageViewMode.value = false
    }

    init {
        setToPageViewMode()
    }
}