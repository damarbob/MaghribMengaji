package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent

class RetrieveQuranPageStudent {

    companion object {
        private val TAG = RetrieveQuranPageStudent::class.simpleName
    }

    operator fun invoke(studentId: String, onQuranPageStudentsRetrieved: (List<QuranPageStudent>) -> Unit) {
        val dbPage = Firebase.firestore.collection(QuranPageStudent.COLLECTION)

        dbPage.whereEqualTo("studentId", studentId).get()
            .addOnSuccessListener { documents ->
                val pageStudents = arrayListOf<QuranPageStudent>()

                for (document in documents) {
                    val quranPageData = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $quranPageData")
                    Log.d(TAG, quranPageData["pageId"].toString())

                    val pageId = (quranPageData["pageId"] as? Long)?.toInt()
                    val ocrscore = (quranPageData["ocrscore"] as? Long)?.toInt()
                    val tidinessScore = (quranPageData["tidinessScore"] as? Long)?.toInt()
                    val accuracyScore = (quranPageData["accuracyScore"] as? Long)?.toInt()
                    val consistencyScore = (quranPageData["consistencyScore"] as? Long)?.toInt()

                    val pageStudent = QuranPageStudent(
                        pageId,
                        quranPageData["studentId"] as String?,
                        quranPageData["teacherId"] as String?,
                        quranPageData["pictureUriString"] as String?,
                        ocrscore,
                        tidinessScore,
                        accuracyScore,
                        consistencyScore
                    )

                    pageStudents.add(pageStudent)
                }

                // Call the lambda function with the list of page students
                onQuranPageStudentsRetrieved(pageStudents)

                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}