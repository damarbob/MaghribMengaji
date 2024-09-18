package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import java.util.Calendar

class MaghribBonusUseCase {

    companion object {
        private val TAG = MaghribBonusUseCase::class.java.simpleName

    }

    operator fun invoke(): Int {
        val time = Calendar.getInstance().timeInMillis
        val maghrib = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val isya = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

//        Log.d("Jaccard", "time: $time")

        return if (time in maghrib..isya) 10 else 0
    }
}