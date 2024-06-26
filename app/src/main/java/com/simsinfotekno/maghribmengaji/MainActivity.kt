package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.jakewharton.processphoenix.ProcessPhoenix
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPages
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.usecase.RetrieveQuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity(), ActivityRestartable {

    private lateinit var binding: ActivityMainBinding

    /* View models */
    private lateinit var viewModel: MainViewModel

    /* Views */
    private lateinit var navController: NavController

    /* Variables */
    private lateinit var connectivityObserver: ConnectivityObserver
    private var connectionStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Unavailable
    private lateinit var alertBuilder: AlertDialog

    /* Use cases */
    private val retrieveUserProfile = RetrieveUserProfile()
    private val retrieveQuranPageStudent = RetrieveQuranPageStudent()

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertBuilder = MaterialAlertDialogBuilder(this)
            .create()


        // TODO: send connection status to view model
        // Check connection status
        checkConnection()
        handleConnectionStatus(connectionStatus)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.maghrib_mengaji_primary)
        setStatusBarTextColor(isLightTheme = false)// Set the status bar text color

        // Register EventBus
//        EventBus.getDefault().register(this)

//        runAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* View models */
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

//        /* Views */
//        val fabVolumeList = binding.mainFabVolumeList
//
//        /* Listeners */
//        fabVolumeList.setOnClickListener {
//            val fragmentId = navController.currentDestination?.id
//            when (fragmentId) {
//                R.id.homeFragment -> navController.navigate(R.id.action_homeFragment_to_volumeListFragment)
//                R.id.pageListFragment -> navController.navigate(R.id.action_pageListFragment_to_volumeListFragment)
//            }
//        }
//        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            when (destination.id) {
//                R.id.homeFragment -> fabVolumeList.show()
//                else -> fabVolumeList.hide()
//            }
//        }

    }

    // Check connection
    private fun checkConnection() {
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        connectivityObserver.observe().onEach {
            connectionStatus = it
            Log.d(TAG, "Status is $it")
            handleConnectionStatus(it)
        }.launchIn(lifecycleScope)
    }

    // Show alert when no connection
    private fun noConnectionAlert() {
        if (connectionStatus != ConnectivityObserver.Status.Available) {

            // Show confirmation dialog
            alertBuilder = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.no_internet))
                .setMessage(getString(R.string.you_have_to_connect_internet))
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
////                    recreate() // Reload activity
//                    checkConnection()
//                    checkConnectionAndShowAlertIfNeeded()
                }
//                .setNeutralButton(getString(R.string.close)) { dialog, which ->
//                    finish() // Exit app
//                }
                .setCancelable(false) // Prevent dismissing by back button
                .create()
                .apply {
                    setCanceledOnTouchOutside(false) // Prevent dismissing by clicking outside
                }
            alertBuilder.show()
        }
    }

//    private fun checkConnectionAndShowAlertIfNeeded() {
//        connectivityObserver = NetworkConnectivityObserver(applicationContext)
//        connectivityObserver.observe().onEach {
//            connectionStatus = it
//            Log.d(TAG, "Status is $it")
//            Toast.makeText(applicationContext, "Internet status $it", Toast.LENGTH_SHORT).show()
//            handleConnectionStatus(it)
//        }.launchIn(lifecycleScope)
//    }

    // Handle connection base on network status
    private fun handleConnectionStatus(status: ConnectivityObserver.Status) {
        when (status) {
            ConnectivityObserver.Status.Available -> {
                runAuthentication()
                alertBuilder.dismiss()
                Toast.makeText(this, getString(R.string.network_status_x, status), Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(this, getString(R.string.network_status_x, status), Toast.LENGTH_SHORT).show()
                noConnectionAlert()
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

        val previousUserName = MaghribMengajiPref.readString(this, MaghribMengajiPref.USER_NAME_KEY)

        Log.d(TAG, "Current user: ${currentUser.displayName} | Previous user: $previousUserName")

        /* Check the current user name in case it's different from the previous user */
        if (currentUser.displayName != previousUserName) {
            MaghribMengajiPref.saveString(
                this,
                MaghribMengajiPref.USER_NAME_KEY,
                currentUser.displayName
            )
            ProcessPhoenix.triggerRebirth(this)
        }

        /* Login checks */
        checkUserEmailVerification(
            { verified ->
                if (!verified) {
                    // If UNVERIFIED

                    // Show confirmation dialog
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.verification))
                        .setMessage(getString(R.string.you_have_to_verify_email))
                        .setNegativeButton(getString(R.string.refresh)) { dialog, which ->

                            restartActivity() // Restart activity

                        }
                        .setPositiveButton(getString(R.string.send)) { dialog, which ->

                            resendVerificationEmail() // Send verification email

                            // Show confirmation dialog
                            MaterialAlertDialogBuilder(this)
                                .setTitle(getString(R.string.verification))
                                .setMessage(getString(R.string.you_have_to_verify_email))
                                .setPositiveButton(getString(R.string.refresh)) { dialog, which ->

                                    restartActivity() // Restart activity

                                }
                                .setNeutralButton(getString(R.string.close)) { dialog, which ->
                                    finish() // Exit app
                                }
                                .setCancelable(false) // Prevent dismissing by back button
                                .create()
                                .apply {
                                    setCanceledOnTouchOutside(false) // Prevent dismissing by clicking outside
                                }
                                .show()

                        }
                        .setNeutralButton(getString(R.string.close)) { dialog, which ->
                            finish() // Exit app
                        }
                        .setCancelable(false) // Prevent dismissing by back button
                        .create()
                        .apply {
                            setCanceledOnTouchOutside(false) // Prevent dismissing by clicking outside
                        }
                        .show()

                } else {
                    // If VERIFIED

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

                        Toast.makeText(
                            this,
                            getString(R.string.login_successful),
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        // Start retrieving user data
                        student.ustadhId?.let { retrieveUstadhProfile(it) }
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
                }
            },
            { exception ->
                Toast.makeText(this, exception, Toast.LENGTH_SHORT)
                    .show()
            }
        )

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

            Log.d(TAG, "Page students imported")
        }

    }

    private fun resendVerificationEmail() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this,
                    getString(R.string.verification_email_sent),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "${getString(R.string.failed_to_send_verification_email)}: ${task.exception?.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
            ?: Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show()
    }

    private fun checkUserEmailVerification(
        onEmailVerified: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isVerified = user.isEmailVerified
                onEmailVerified(isVerified)
            } else {
                val errorMessage = task.exception?.localizedMessage ?: "Failed to reload user."
                onFailure(errorMessage)
            }
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