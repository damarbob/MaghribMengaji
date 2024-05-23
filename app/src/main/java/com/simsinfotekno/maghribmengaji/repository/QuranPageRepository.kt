package com.simsinfotekno.maghribmengaji.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.simsinfotekno.maghribmengaji.model.QuranPage

class QuranPageRepository() : Repository<QuranPage>() {
    override fun onStart() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
//        TODO("Not yet implemented")
    }

    override fun createRecord(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordAdded(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordDeleted(record: QuranPage) {
//        TODO("Not yet implemented")
    }

    override fun onRecordCleared() {
        TODO("Not yet implemented")
    }

    fun getRecordById(id: Int?): QuranPage? {
        for (r in getRecords()) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }

    fun getRecordByIds(ids: IntArray): List<QuranPage> {
        val result: ArrayList<QuranPage> = arrayListOf()

        ids.forEach { i ->
            result.add(getRecordById(i)!!)
        }

        return result
    }

    // Get image based on quranPages id from Firestore
    fun getFirebaseRecordById(
        idValue: Int,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("quranPages").whereEqualTo("id", idValue).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val imageUrl = document.getString("picture")
                    if (imageUrl != null) {
                        onSuccess(imageUrl)
                    } else {
                        onFailure(Exception("Image URL not found"))
                    }
                } else {
                    onFailure(Exception("No document found with id = $idValue"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
