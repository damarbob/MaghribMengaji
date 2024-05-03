package com.simsinfotekno.maghribmengaji.ui.page

import androidx.lifecycle.ViewModel
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository

class PageViewModel : ViewModel() {
    private var volume: Int = 0
    private var page: Int = 0

    // Repository
    val quranPageRepository = QuranPageRepository()
}