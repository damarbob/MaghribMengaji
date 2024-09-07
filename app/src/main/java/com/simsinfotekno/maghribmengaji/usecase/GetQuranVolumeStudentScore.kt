package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.model.QuranVolumeStudentScore

class GetQuranVolumeStudentScore {

    companion object {
        private val TAG = GetQuranVolumeStudentScore::class.simpleName
    }

    // Method to retrieve the user profile
    operator fun invoke(
        userId: String,
        volumeId: Int,
        onQuranVolumeStudentScoreRetrieved: (Int) -> Unit
    ) {
        val db = Firebase.firestore.collection(QuranPageStudent.COLLECTION)
//        Log.d(TAG, "quranPages ${MainApplication.quranVolumes[volumeId - 1].pageIds}")
        val quranPages = MainApplication.quranVolumes[volumeId - 1].pageIds
        val quranPageStudent: ArrayList<Int> = arrayListOf()
        var score = 0
        val quranVolumeStudentScore: QuranVolumeStudentScore

        val studentsQuranPage = quranPageStudentRepository.getRecordByIdsNoStrict(quranPages.toIntArray())

        for (quranPage in studentsQuranPage) {
            if (quranPage.oCRScore != null) {
                score += quranPage.oCRScore!!
            }
        }

        if (!studentsQuranPage.isEmpty()) score /= studentsQuranPage.size
        // Call the lambda function with the retrieved score
        onQuranVolumeStudentScoreRetrieved(score)

        quranVolumeStudentScore = QuranVolumeStudentScore(
            studentId = userId,
            volumeId = volumeId,
            quranPageStudent = quranPageStudent,
            score = score,
        )
    }

}