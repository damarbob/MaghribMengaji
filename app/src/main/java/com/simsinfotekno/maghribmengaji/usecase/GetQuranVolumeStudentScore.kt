package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.model.QuranVolumeStudentScore

class GetQuranVolumeStudentScore {

    companion object {
        private val TAG = GetQuranVolumeStudentScore::class.simpleName
    }

    // Method to retrieve the user profile
    operator fun invoke(userId: String, volumeId: Int, onQuranVolumeStudentScoreRetrieved: (Int) -> Unit) {
        val db = Firebase.firestore.collection(QuranPageStudent.COLLECTION)
        Log.d(TAG, "quranPages ${MainApplication.quranVolumes[volumeId-1].pageIds}")
        val quranPages = MainApplication.quranVolumes[volumeId-1].pageIds
        val quranPageStudent: ArrayList<Int> = arrayListOf()
        var score = 0
        val quranVolumeStudentScore: QuranVolumeStudentScore

        for (quranPage in quranPages)
        db.whereEqualTo("id", userId).whereEqualTo("pageId", quranPage).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    // Get quranPage data
                    val data = document.data

                    score += data["ocrscore"] as Int
                    quranPageStudent.add(data["pageId"] as Int)
//                    Log.d(TAG, "Document found with ID: ${document.id} => $data")
                }

                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        Log.d(TAG, "the volume score is $score")
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