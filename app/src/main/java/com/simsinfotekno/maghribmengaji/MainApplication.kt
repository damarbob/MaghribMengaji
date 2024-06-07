package com.simsinfotekno.maghribmengaji

import android.app.Application
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranRecordingStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranVolumeRepository
import com.simsinfotekno.maghribmengaji.repository.StudentRepository

class MainApplication: Application() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName

        // Repository
        val quranVolumeRepository = QuranVolumeRepository()
        val quranPageRepository = QuranPageRepository()
        val quranPageStudentRepository = QuranPageStudentRepository()
        val studentRepository = StudentRepository()
        val quranRecordingStudentRepository = QuranRecordingStudentRepository()
    }
    override fun onCreate() {
        super.onCreate()

//        Log.d(TAG, "onCreate")
    }
}