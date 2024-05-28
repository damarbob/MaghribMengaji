package com.simsinfotekno.maghribmengaji

import android.app.Application

class MainApplication: Application() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName
    }
    override fun onCreate() {
        super.onCreate()

//        Log.d(TAG, "onCreate")
    }
}