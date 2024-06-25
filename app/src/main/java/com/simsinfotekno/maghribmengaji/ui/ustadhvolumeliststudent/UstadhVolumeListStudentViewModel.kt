package com.simsinfotekno.maghribmengaji.ui.ustadhvolumeliststudent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranVolumeStudentRepository
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.usecase.RemoveUstadhUseCase
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile

class UstadhVolumeListStudentViewModel : ViewModel() {

    companion object {
        private val TAG = UstadhVolumeListStudentViewModel::class.java.simpleName
    }

    private val _getStudentPageResult = MutableLiveData<Result<List<QuranPageStudent>>?>()
    val getStudentPageResult: LiveData<Result<List<QuranPageStudent>>?> get() = _getStudentPageResult

    private val _getStudentProfileResult = MutableLiveData<MaghribMengajiUser>(null)
    val getStudentProfileResult: LiveData<MaghribMengajiUser> get() = _getStudentProfileResult

    /* Use cases */
    private val retrieveUserProfile = RetrieveUserProfile()

    fun getStudentProfile(studentId: String) {
        retrieveUserProfile(studentId) {
            _getStudentProfileResult.value = it
        }
    }

    fun getStudentPageData(studentUserId: String) {
        Log.d(TAG, "Requesting page data for student $studentUserId")

        val db = Firebase.firestore.collection(QuranPageStudent.COLLECTION)
        db.whereEqualTo("studentId", studentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->

                val pages = arrayListOf<QuranPageStudent>()

                for (document in documents) {

                    // Get user data
                    val pageStudent = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $pageStudent")

                    val page = QuranPageStudent(
                        pageId = (pageStudent["pageId"] as? Long)?.toInt(),
                        studentId = pageStudent["studentId"] as? String ?: "",
                        teacherId = pageStudent["teacherId"] as? String ?: "",
                        pictureUriString = pageStudent["pictureUriString"] as? String ?: "",
                        oCRScore = (pageStudent["ocrscore"] as? Long)?.toInt(),
                        tidinessScore = (pageStudent["tidinessScore"] as? Long)?.toInt(),
                        accuracyScore = (pageStudent["accuracyScore"] as? Long)?.toInt(),
                        consistencyScore = (pageStudent["consistencyScore"] as? Long)?.toInt(),
                        createdAt = pageStudent["createdAt"] as? Timestamp
                    )

                    pages.add(page)

                }
                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }

                // From here on, page students are available and accessible
                ustadhQuranPageStudentRepository.setRecords(pages, false)

                // Use the retrieved page students to get the volumes
                ustadhQuranVolumeStudentRepository.setRecords(getStudentVolumes(), false)

                _getStudentPageResult.value = Result.success(pages) // Must be the last

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                _getStudentPageResult.value = Result.failure(Exception("Error retrieving page list"))
            }
    }

    // RUN ONLY IF THE ustadhQuranPageStudentRepository.getRecords() IS AVAILABLE
    private fun getStudentVolumes(): List<QuranVolume> {

        val volumes = arrayListOf<QuranVolume>()
        val quranPageStudents = ustadhQuranPageStudentRepository.getRecords()

        // Map to store volume IDs and corresponding QuranVolumes
        val volumeMap = mutableMapOf<Int, QuranVolume>()

        // Iterate through each page student and assign volumes
        quranPageStudents.forEach { pageStudent ->
            val pageId = pageStudent.pageId

            quranVolumes.forEach { volume ->
                if (pageId != null && volume.pageIds.contains(pageId)) {
                    volumeMap[volume.id] = volume
                }
            }
        }

        // Collect unique volumes
        volumes.addAll(volumeMap.values)

        return volumes

    }

}