package com.simsinfotekno.maghribmengaji

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
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
import com.simsinfotekno.maghribmengaji.event.OnMainActivityFeatureRequest
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.usecase.NetworkConnectivityUseCase
import com.simsinfotekno.maghribmengaji.usecase.RetrieveQuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity(), ActivityRestartable {

    private lateinit var binding: ActivityMainBinding

    /* View models */
    private lateinit var viewModel: MainViewModel

    /* Views */
    private lateinit var navController: NavController

    /* Variables */
    private lateinit var connectivityObserver: ConnectivityObserver
    private var connectionStatus: ConnectivityObserver.Status =
        ConnectivityObserver.Status.Unavailable
    private lateinit var alertBuilder: AlertDialog

    /* Use cases */
    private val retrieveUserProfile = RetrieveUserProfile()
    private val retrieveQuranPageStudent = RetrieveQuranPageStudent()
    private lateinit var networkConnectivityUseCase: NetworkConnectivityUseCase

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the status bar color
//        window.statusBarColor = ContextCompat.getColor(this, R.color.md_theme_primary)
//        setStatusBarTextColor(isLightTheme = false)// Set the status bar text color

        // Register EventBus
        EventBus.getDefault().register(this)

//        runAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* View models */
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Check connection status
        networkConnectivityUseCase = NetworkConnectivityUseCase(applicationContext)
        checkConnection()
        viewModel.connectionStatus.observe(this) {
            handleConnectionStatus(it)
            Log.d(TAG, it.toString())
        }

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        /* Views */
        val mainNavigationHeader =
            binding.mainNavigationView.inflateHeaderView(R.layout.header_drawer_main)

        mainNavigationHeader.findViewById<TextView>(R.id.headerMainName).text =
            Firebase.auth.currentUser?.displayName
        mainNavigationHeader.findViewById<TextView>(R.id.headerMainEmail).text =
            Firebase.auth.currentUser?.email


        // Show confirmation dialog
        alertBuilder = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.no_internet))
            .setMessage(getString(R.string.you_have_to_connect_internet))
            .setPositiveButton(getString(R.string.yes), null)
            .setCancelable(false) // Prevent dismissing by back button
            .create()
            .apply {
                setCanceledOnTouchOutside(false) // Prevent dismissing by clicking outside
            }

        // Hide some menu from navigation menu
        setInternetRequiredMenusVisible(false)

        /* Listeners */
        binding.mainNavigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_volume_list -> {
                    navController.navigate(R.id.action_global_volumeListFragment)
                }
                R.id.menu_chapter_list -> {
                    navController.navigate(R.id.action_global_chapterListFragment)
                }
                R.id.menu_juz_list -> {
                    navController.navigate(R.id.action_global_juzListFragment)
                }
                R.id.menu_tc -> {

                    // Show tc confirmation
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.are_you_sure))
                        .setMessage(getString(R.string.you_will_open_an_external_link_using_browser))
                        .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.yes)) { dialog, which ->

                            // Open terms and conditions in browser
                            val browserIntent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getString(R.string.terms_and_conditions_url))
                                )
                            startActivity(browserIntent)

                        }
                        .show()

                }

                R.id.menu_contact -> {

                    // Show email confirmation
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.are_you_sure))
                        .setMessage(getString(R.string.send_email_to_the_developer))
                        .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                            openEmailClient() // Open email client app
                        }
                        .show()

                }

                R.id.menu_about -> {

                    // Show application info dialog
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.about))
                        .setMessage(getString(R.string.app_about_dialog))
                        .setNeutralButton(resources.getString(R.string.close)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }

                R.id.menu_sign_out -> {

                    // Show sign out confirmation dialog
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.are_you_sure))
                        .setMessage(getString(R.string.you_will_be_logged_out_from_this_account))
                        .setNeutralButton(resources.getString(R.string.close)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.yes)) { dialog, which ->

                            signOut()

                        }
                        .show()

                }
            }
            binding.container.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }

    }

    private fun signOut() {
        // Sign out and navigate to login

        Firebase.auth.signOut() // Sign out from firebase

        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent the user from coming back to it
    }

    // Check connection
    private fun checkConnection() {
        networkConnectivityUseCase(this, onAvailableNetwork = {
            viewModel.networkAvailable()
            Log.d(TAG, viewModel.connectionStatus.value.toString())
        }, onUnavailableNetwork = {
            viewModel.networkUnavailable()
            Log.d(TAG, viewModel.connectionStatus.value.toString())
        })
    }

    // Show alert when no connection
    private fun noConnectionAlert() {
        Toast.makeText(
            this,
            getString(R.string.network_status_x, connectionStatus),
            Toast.LENGTH_SHORT
        ).show()
        alertBuilder.show()
    }

    // Handle connection base on network status
    private fun handleConnectionStatus(status: ConnectivityObserver.Status) {
        when (status) {
            ConnectivityObserver.Status.Available -> {

                // If connection is available
                runAuthentication()
                alertBuilder.dismiss()

                // Show internet required menus
                setInternetRequiredMenusVisible(true)

            }
            else -> {
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
        EventBus.getDefault().unregister(this)

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
                                .setNeutralButton(getString(R.string.sign_out)) { dialog, which ->
                                    signOut() // Sign out
                                }
                                .setCancelable(false) // Prevent dismissing by back button
                                .create()
                                .apply {
                                    setCanceledOnTouchOutside(false) // Prevent dismissing by clicking outside
                                }
                                .show()

                        }
                        .setNeutralButton(getString(R.string.sign_out)) { dialog, which ->
                            signOut() // Sign out
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
                Toast.makeText(this, exception, Toast.LENGTH_LONG)
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
                studentRepository.getStudent()?.let { student ->
                    OnUserDataLoaded(
                        student,
                        UserDataEvent.PAGE
                    )
                }
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

    private fun openEmailClient() {
        // Define the email address
        val email = getString(R.string.app_email_address)
        val mailto = "mailto:"

        // Create an intent
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(mailto)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback Maghrib Mengaji App")
        }

        // Check if there's an email app available
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            // Show a message if no email app is available
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    // Set the visibility of internet required menu items
    private fun setInternetRequiredMenusVisible(isVisible: Boolean) {
        binding.mainNavigationView.menu.findItem(R.id.menu_volume_list).setVisible(isVisible)
        binding.mainNavigationView.menu.findItem(R.id.menu_chapter_list).setVisible(isVisible)
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

//        if (role == MaghribMengajiUser.ROLE_TEACHER) {
//
//            val navGraph = navController.navInflater.inflate(R.navigation.nav_main)
//            navGraph.setStartDestination(R.id.ustadhHomeFragment)
//            navController.graph = navGraph
//
//            // Navigate to the ustadhHomeFragment
//            navController.navigate(R.id.ustadhHomeFragment)
//
//        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun _053803052023(event: OnMainActivityFeatureRequest) {
        when (event.event) {
            OnMainActivityFeatureRequest.Event.OPEN_DRAWER -> binding.container.openDrawer(
                GravityCompat.START
            )
        }
    }

}