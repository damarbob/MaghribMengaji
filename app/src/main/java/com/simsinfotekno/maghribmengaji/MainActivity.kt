package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiStudent
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranVolumeRepository


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        val TAG: String = MainActivity::class.java.simpleName

        // Test user setup
        val testStudent = MaghribMengajiStudent(
            "1",
            "Damar Maulana",
            "ibn.damr@gmail.com",
            10,
            listOf(1, 2, 3, 4, 5, 6, 7),
            listOf(1, 2, 3, 10),
            listOf(),
        )

        // Repository
        val quranVolumeRepository = QuranVolumeRepository()
        val quranPageRepository = QuranPageRepository()
        val quranPageStudentRepository = QuranPageStudentRepository()

        // Test data set
        val quranVolumes = listOf(
            QuranVolume(1, "1", (1..61).toList()),
            QuranVolume(2, "2", (62..121).toList()),
            QuranVolume(3, "3", (122..181).toList()),
            QuranVolume(4, "4", (182..241).toList()),
            QuranVolume(5, "5", (242..301).toList()),
            QuranVolume(6, "6", (302..361).toList()),
            QuranVolume(7, "7", (362..421).toList()),
            QuranVolume(8, "8", (422..481).toList()),
            QuranVolume(9, "9", (482..541).toList()),
            QuranVolume(10, "10", (542..604).toList()),
        )
        val quranPages = (1..604).map { QuranPage(it, "$it") }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase tests
//        testFirestore()
//        testFirestoreQuranPage()

        // Test data insertion to repository
        quranVolumeRepository.setRecords(quranVolumes, false)
        quranPageRepository.setRecords(quranPages, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // Views
        val fabVolumeList = binding.mainFabVolumeList

        // Listener
        fabVolumeList.setOnClickListener {
            val fragmentId = navController.currentDestination?.id
            when (fragmentId) {
                R.id.homeFragment -> navController.navigate(R.id.action_homeFragment_to_volumeListFragment)
                R.id.pageListFragment -> navController.navigate(R.id.action_pageListFragment_to_volumeListFragment)
            }
//            navController.navigate(R.id.action_homeFragment_to_volumeListFragment)
            // Testing
//            navController.navigate(R.id.action_homeFragment_to_pageFragment)
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.volumeListFragment -> fabVolumeList.hide()
                R.id.pageFragment -> fabVolumeList.hide()
                R.id.similarityScoreFragment -> fabVolumeList.hide()
                R.id.profileFragment -> fabVolumeList.hide()
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