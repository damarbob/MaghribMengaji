package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.util.Log
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

    /**
     * Calculate submit streak bonus
     * @return list of streak and bonus
     * */
class SubmitStreakBonusUseCase {

    companion object {
        private val TAG = SubmitStreakBonusUseCase::class.java.simpleName
    }

        operator fun invoke(): List<Float> {
            val student = MainApplication.studentRepository.getStudent() ?: return listOf(1f, 1f)
            val lastDailySubmit = student.lastDailySubmit?.toDate()?.time ?: return listOf(1f, 1f)
            val currentStreak = student.currentSubmitStreak ?: 1

            val today = Calendar.getInstance()
            val lastSubmitDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
                timeInMillis = lastDailySubmit
            }

            // No bonus if the user submitted today
            if (isSameDay(today, lastSubmitDate)) {
                return listOf(currentStreak.toFloat(), 1f)
            }

            // Calculate new streak and bonus
            val newStreak = if (isYesterday(lastSubmitDate, today)) currentStreak + 1 else 1
            val bonusMultiplier = when (newStreak) {
                1 -> 1f
                2 -> 1.02f
                3 -> 1.05f
                4 -> 1.07f
                5 -> 1.1f
                6 -> 1.15f
                else -> 2f
            }

            return listOf(newStreak.toFloat(), bonusMultiplier)
        }


        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        cal2.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(cal1, cal2)
    }
}