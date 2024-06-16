package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPages
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /* View model */
    private lateinit var viewModel: MainViewModel

    private lateinit var navController: NavController

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register EventBus
//        EventBus.getDefault().register(this)

        runAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* View models */
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        /* Views */
        val fabVolumeList = binding.mainFabVolumeList

        /* Listeners */
        fabVolumeList.setOnClickListener {
            val fragmentId = navController.currentDestination?.id
            when (fragmentId) {
                R.id.homeFragment -> navController.navigate(R.id.action_homeFragment_to_volumeListFragment)
                R.id.pageListFragment -> navController.navigate(R.id.action_pageListFragment_to_volumeListFragment)
            }
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.homeFragment -> fabVolumeList.show()
                else -> fabVolumeList.hide()
            }
        }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister EventBus
//        EventBus.getDefault().unregister(this)

    }

    private fun runAuthentication() {
        /* Auth */
        val auth = Firebase.auth // Initialize Firebase Auth
        val currentUser = auth.currentUser ?: return // Get current user

        /* Get user data */

        // Get profile data
        val db = Firebase.firestore.collection(MaghribMengajiUser.COLLECTION)
        db.whereEqualTo("id", currentUser.uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    // Get user data
                    val data = document.data

                    Log.d(TAG, "Document found with ID: ${document.id} => $data")

                    val student = MaghribMengajiUser(
                        currentUser.uid,
                        currentUser.displayName,
                        currentUser.email,
                        role = data["role"] as String?,
                        lastPageId = data["lastPageId"] as Int?,
                        ustadhId = data["ustadhId"] as String?,
                    )

                    studentRepository.setStudent(student)

                    // Post a user data loaded event
                    EventBus.getDefault().post(
                        OnUserDataLoaded(
                            student,
                            UserDataEvent.PROFILE
                        )
                    )

                    Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT)
                        .show()

                    // Start retrieving user data
                    retrieveUserData(currentUser.uid)

                    student.role?.let { // If role is not null
                        // Adjust nav graph based on the user's role
                        adjustNavGraph(it)
                        Log.d(TAG, "User's role: $it")

                        // Navigate to ustadh selection if ustadhId is null
                        if (it == MaghribMengajiUser.ROLE_STUDENT && student.ustadhId == null) {
                            navController.navigate(R.id.action_global_ustadhListFragment)
                        }
                    }

                }
                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        /* End of auth */
    }

    private fun retrieveUserData(uid: String) {

        // Get quran page data
        val dbPage = Firebase.firestore.collection(QuranPageStudent.COLLECTION)
        dbPage.whereEqualTo("studentId", uid).get()
            .addOnSuccessListener { documents ->

                val pageStudents = arrayListOf<QuranPageStudent>()

                for (document in documents) {

                    // Get quran page data
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
                MainApplication.quranPageStudentRepository.setRecords(pageStudents, true)

                // Post a user data loaded event
                EventBus.getDefault().post(
                    OnUserDataLoaded(
                        studentRepository.getStudent(),
                        UserDataEvent.PAGE
                    )
                )

                if (documents.isEmpty) {
                    Log.d(TAG, "No matching documents found.")
                }

                Toast.makeText(this, "Data imported", Toast.LENGTH_SHORT)
                    .show()

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

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

    private fun adjustNavGraph(role: String) {

        if (role == MaghribMengajiUser.ROLE_TEACHER) {

            val navGraph = navController.navInflater.inflate(R.navigation.nav_main)
            navGraph.setStartDestination(R.id.ustadhHomeFragment)
            navController.graph = navGraph

            // Navigate to the ustadhHomeFragment
            navController.navigate(R.id.ustadhHomeFragment)

        }

    }

}