package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.model.QuranVolume


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val testUser = MaghribMengajiUser("Damar Maulana", "ibn.damr@gmail.com", 10)
        val quranVolumes = listOf(QuranVolume(1, "1"), QuranVolume(2, "2"), QuranVolume(3, "3"))
        val quranPages = listOf(QuranPage(100,"100"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        testFirestore() //
        testFirestoreQuranPage() //

        // Test user setup
        testUser.finishedPageIds = listOf(0, 1, 2, 3, 4)
        testUser.achievementIds = listOf()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // Views
        val fabVolumeList = binding.mainFabVolumeList

        // Listener
        fabVolumeList.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_volumeListFragment)
            // Testing
//            navController.navigate(R.id.action_homeFragment_to_pageFragment)
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.volumeListFragment -> fabVolumeList.hide()
                else -> fabVolumeList.show()
            }
        }

    }

    override fun onStart() {
        super.onStart()

    }

    private fun testFirestore() {
        // Initialize
        val db = Firebase.firestore
        Log.d(TAG, "db = $db")

        // Create a batch object
        val batch = db.batch()

        // Iterate over your list of data class objects and add them to the batch
        for (quranVolume in quranVolumes) {
            val docRef = db.collection("quranVolumes").document()
            batch.set(docRef, quranVolume)
        }

        // Commit the batched write operation
        batch.commit().addOnSuccessListener {
            println("Batch write operation completed successfully.")
        }

        // Add a new document with a generated ID
        db.collection("quranVolumes")
            .add(quranVolumes[0])
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun testFirestoreQuranPage() {
        // Initialize
        val db = Firebase.firestore
        Log.d(TAG, "db = $db")

        // Create a batch object
        val batch = db.batch()

        // Iterate over your list of data class objects and add them to the batch
        for (quranPages in quranPages) {
            val docRef = db.collection("quranPages").document()
            batch.set(docRef, quranPages)
        }

        // Commit the batched write operation
        batch.commit().addOnSuccessListener {
            println("Batch write operation completed successfully.")
        }

        // Add a new document with a generated ID
        db.collection("quranPages")
            .add(quranPages[0])
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}