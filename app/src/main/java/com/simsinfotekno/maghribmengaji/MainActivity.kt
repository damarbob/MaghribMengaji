package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPages
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.usecase.RetrieveQuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /* View models */
    private lateinit var viewModel: MainViewModel

    private lateinit var navController: NavController

    /* Use cases */
    private val retrieveUserProfile = RetrieveUserProfile()
    private val retrieveQuranPageStudent = RetrieveQuranPageStudent()

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.maghrib_mengaji_primary)
        setStatusBarTextColor(isLightTheme = false)// Set the status bar text color

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

    private fun setStatusBarTextColor(isLightTheme: Boolean) {
        val decor = window.decorView
        if (isLightTheme) {
            decor.systemUiVisibility =
                decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
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
        retrieveUserProfile(currentUser.uid) { student ->

            // Actions to perform when the user profile is retrieved
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
            retrieveUstadhProfile(student.ustadhId!!)
            retrieveQuranPageStudent(student.id!!)

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

        /* End of auth */
    }

    private fun retrieveUstadhProfile(ustadhId: String) {
        retrieveUserProfile(ustadhId) { ustadh ->
            studentRepository.setUstadh(ustadh)
        }
    }

    private fun retrieveQuranPageStudent(uid: String) {

        // Get quran page data
        retrieveQuranPageStudent(uid) { pageStudents ->
            // Actions to perform when the list of QuranPageStudents is retrieved
            quranPageStudentRepository.setRecords(pageStudents, true)

            // Post a user data loaded event
            EventBus.getDefault().post(
                OnUserDataLoaded(
                    studentRepository.getStudent(),
                    UserDataEvent.PAGE
                )
            )

            Toast.makeText(this, "Data imported", Toast.LENGTH_SHORT).show()
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