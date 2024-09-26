package com.simsinfotekno.maghribmengaji

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.jakewharton.processphoenix.ProcessPhoenix
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPages
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.transactionRepository
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnMainActivityFeatureRequest
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.ui.ImagePickerBottomSheetDialog
import com.simsinfotekno.maghribmengaji.ui.infaq.InfaqFragment
import com.simsinfotekno.maghribmengaji.ui.similarityscore.SimilarityScoreFragment
import com.simsinfotekno.maghribmengaji.ui.similarityscore.SimilarityScoreFragment.Companion
import com.simsinfotekno.maghribmengaji.usecase.CancelDailyNotificationUseCase
import com.simsinfotekno.maghribmengaji.usecase.ExtractQRCodeToPageIdUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchCameraUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchGalleryUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.LoadBitmapFromUri
import com.simsinfotekno.maghribmengaji.usecase.NetworkConnectivityUseCase
import com.simsinfotekno.maghribmengaji.usecase.QRCodeScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.RequestPermissionsUseCase
import com.simsinfotekno.maghribmengaji.usecase.RetrieveQuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserProfile
import com.simsinfotekno.maghribmengaji.usecase.RetrieveUserTransactions
import com.simsinfotekno.maghribmengaji.usecase.ScheduleDailyNotificationUseCase
import com.simsinfotekno.maghribmengaji.utils.BitmapToUriUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity(), ActivityRestartable,
    ActivityResultCallback<ActivityResult> {

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
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickMediaModernLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                when {
                    !isGranted -> {
                        // Handle the case where the user denied the permission
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.permission_is_not_granted, permission),
                            Toast.LENGTH_LONG
                        )
                    }

                    shouldShowRequestPermissionRationale(permission) -> {
                        // Permission denied without "Don't ask again" - Show rationale
                        showPermissionRationale(permission)
                    }

                    else -> {
                        // Permission denied with "Don't ask again" - Guide user to settings
                        showPermissionSettingsDialog()
                    }
                }
            }
        }
    private lateinit var alarmManager: AlarmManager

    /* Use cases */
    private val requestPermissionsUseCase = RequestPermissionsUseCase()
    private val retrieveUserProfile = RetrieveUserProfile()
    private val retrieveQuranPageStudent = RetrieveQuranPageStudent()
    private val retrieveUserTransactions = RetrieveUserTransactions()
    private lateinit var networkConnectivityUseCase: NetworkConnectivityUseCase
    private val launchScannerUseCase = LaunchScannerUseCase()
    private val launchCameraUseCase = LaunchCameraUseCase()
    private val launchGalleryUseCase = LaunchGalleryUseCase()
    private val loadBitmapFromUri = LoadBitmapFromUri()
    private val qrCodeScannerUseCase = QRCodeScannerUseCase()
    private val extractQRCodeToPageIdUseCase = ExtractQRCodeToPageIdUseCase()

    /* Notification */
    private val scheduleDailyNotificationUseCase = ScheduleDailyNotificationUseCase()
    private val cancelDailyNotificationUseCase = CancelDailyNotificationUseCase()

    /* Billing */
    private lateinit var myBilled: BillingClient
    private val courseList = listOf("")

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set schedule notif
        val sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
        val isNotificationsEnabled =
            sharedPreferences.getBoolean(MaghribMengajiPref.NOTIF_ENABLED_KEY, true)

        // Request notification permission
//        if (!requestPermissionsUseCase.hasPostNotificationPermission(this) && !requestPermissionsUseCase.hasScheduleExactAlarmPermission(this)) {
//            requestPermissionsUseCase(requestPermissionsLauncher, this, requestPermissionsUseCase.getNotificationPermissions())
//        }
        if (!requestPermissionsUseCase.hasPostNotificationPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionsUseCase(
                    requestPermissionsLauncher,
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                )
            }
        }

        alarmManager = this.getSystemService<AlarmManager>()!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when {
                alarmManager.canScheduleExactAlarms() -> {
                    if (isNotificationsEnabled) {
                        scheduleDailyNotificationUseCase(this) // Enable notifications
                    } else {
                        cancelDailyNotificationUseCase(this)// Disable notifications
                    }
                }

                else -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.need_permission))
                        .setMessage(getString(R.string.you_need_to_grant_permission_to_get_maghrib_mengaji_notification))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                        .setNegativeButton(getString(R.string.no)) { _, _ ->
                            cancelDailyNotificationUseCase(this)
                            sharedPreferences.edit().apply {
                                putBoolean(MaghribMengajiPref.NOTIF_ENABLED_KEY, false)
                                apply()
                            }
                        }
                }
            }
        }
        // setup for IAP
//        private fun doMyBiller() {
//            myBiller = MyBiller.newBuilder(this)
//                .enablePendingPurchases()
//                .setListener(this)
//                .build()
//            myBiller.startConnection(object : MyBillerStateListener {
//                override fun onBillingSetupFinished(billingResult: BillingResult) {
//                    if (billingResult.responseCode == MyBiller.BillingResponseCode.OK) {
//                        // The MyBiller is setup successfully
//                    }
//                }
//
//                override fun onBillingServiceDisconnected() {
//                    // Do something, like restarting the billing service
//
//                }
//            })


        // Set the status bar color
//        window.statusBarColor = ContextCompat.getColor(this, R.color.md_theme_primary)
//        setStatusBarTextColor(isLightTheme = false)// Set the status bar text color

        // Register EventBus
        org.greenrobot.eventbus.EventBus.getDefault().register(this)

//        runAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupScanLaunchers()

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

                R.id.menu_donation -> {
//                    navController.navigate(R.id.action_global_paymentFragment)
                    InfaqFragment().show(
                        supportFragmentManager,
                        InfaqFragment::class.java.simpleName
                    )
                }

                R.id.menu_edit_profile -> {
                    navController.navigate(R.id.action_global_editProfileFragment)
                }

                R.id.menu_balance -> {
                    navController.navigate(R.id.action_global_withdrawalFragment)
                }

                R.id.menu_referral_code -> {
                    navController.navigate(R.id.action_global_refferalCodeFragment)
                }

                R.id.menu_setting -> {
                    navController.navigate(R.id.action_global_settingFragment)

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

        viewModel.studentLiveData.observe(this) { maghribMengajiUser ->
            // Only show bottom nav if the role is student
            binding.mainBottomNavigationView.visibility =
                if (maghribMengajiUser.role == MaghribMengajiUser.ROLE_STUDENT) View.VISIBLE else View.GONE

            // Set visibility of bottom nav
            navController.addOnDestinationChangedListener { _, navDestination, _ ->
                val destinationIds =
                    setOf(R.id.volumeListFragment, R.id.pageListFragment, R.id.pageFragment)
                when (navDestination.id) {
                    R.id.similarityScoreFragment -> {
                        binding.mainBottomNavigationView.visibility = View.GONE
                    }

                    in destinationIds -> {
                        binding.mainBottomNavigationView.menu.findItem(R.id.menu_bottom_nav_volume).isChecked =
                            true
                        binding.mainBottomNavigationView.visibility =
                            if (maghribMengajiUser.role == MaghribMengajiUser.ROLE_STUDENT) View.VISIBLE else View.GONE
                    }

                    else -> {
//                    binding.mainBottomNavigationView.selectedItemId = R.id.menu_bottom_nav_home
                        binding.mainBottomNavigationView.menu.findItem(R.id.menu_bottom_nav_home).isChecked =
                            true
                        binding.mainBottomNavigationView.visibility =
                            if (maghribMengajiUser.role == MaghribMengajiUser.ROLE_STUDENT) View.VISIBLE else View.GONE
                    }
                }
            }

            //
            if (maghribMengajiUser.role == MaghribMengajiUser.ROLE_STUDENT)
                supportFragmentManager.fragments.forEach {
                    if (it.id == R.id.similarityScoreFragment) {
                        val layoutParams =
                            it.requireView().layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.setMargins(0, 0, 0, 0)
                        it.requireView().layoutParams = layoutParams
                    } else {
                        val layoutParams =
                            it.requireView().layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.setMargins(0, 0, 0, 200)
                        it.requireView().layoutParams = layoutParams
                    }
                }
        }


        binding.mainBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_bottom_nav_home -> {
                    navController.navigate(R.id.action_global_homeFragment)
                    true
                }

                R.id.menu_bottom_nav_scan -> {
                    startScan()
                    true
                }

                R.id.menu_bottom_nav_volume -> {
                    navController.navigate(R.id.action_global_volumeListFragment)
                    true
                }

                else -> false
            }
        }

        binding.mainBottomNavigationView.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_bottom_nav_home -> {
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        // If home menu reselected in home fragment, refresh data
                        viewModel.refreshData()
                    } else {
                        navController.navigate(R.id.action_global_homeFragment)
                    }
                }

                R.id.menu_bottom_nav_volume -> {
                    if (navController.currentDestination?.id != R.id.volumeListFragment) {
                        navController.navigate(R.id.action_global_volumeListFragment)
                    }
                }
            }
        }
    }

    private fun setupScanLaunchers() {
        // Initialize the scanner launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(), this
        )

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    val imageBitmap = it.data?.extras?.get("data") as Bitmap
                    val imageUri = BitmapToUriUtil.saveBitmapToFile(this, imageBitmap)

                    // Submit image to similarity fragment
                    submitImage(imageUri)

                } else if (it.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(
                        this,
                        getString(R.string.error_scanner_cancelled),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_default_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchCameraUseCase(
                        this,
                        cameraLauncher,
                    )
                } else {
                    Toast.makeText(
                        this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT
                    ).show()
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    val imageUri = it.data?.data

                    // Submit image to similarity fragment
                    submitImage(imageUri)

                } else if (it.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(
                        this,
                        getString(R.string.error_scanner_cancelled),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_default_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        requestGalleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchGalleryUseCase(
                        this, galleryLauncher
                    )
                } else {
                    Toast.makeText(
                        this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // For Android 14+ (modern picker)
        pickMediaModernLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let {
                    submitImage(it)
                }
            }
    }

    private fun submitImage(imageUri: Uri?) {
        Log.d(TAG, "imageuri: $imageUri")
        val bitmap = imageUri?.let { loadBitmapFromUri(this, it) }

        // Check if bitmap is not null
        if (bitmap != null) {
            qrCodeScannerUseCase(bitmap, onBarcodeSuccess = { result ->
                if (result != null) {
                    val pageId = extractQRCodeToPageIdUseCase(result)

                    // If page id not null and in between 1 and 604, navigate to similarity score fragment
                    if (pageId != null && pageId in 1..604) {
                        // Bundle to pass the data
                        val bundle = Bundle().apply {
                            putString(
                                "imageUriString", imageUri.toString()
                            )
                            putInt("pageId", pageId)
                        }

                        // Navigate to the ResultFragment with the Bundle
                        navController.navigate(
                            R.id.action_global_similarityScoreFragment, bundle
                        )
                    } else {
                        // If pageId is not found or not match
                        showQRCodeError(
                            getString(R.string.qr_code_not_detected), getString(
                                R.string.make_sure_the_qr_code_is_clearly_visible_in_the_photo
                            )
                        )
                    }
                } else {
                    // If QR Code not found
                    showQRCodeError(
                        getString(R.string.qr_code_not_detected), getString(
                            R.string.make_sure_the_qr_code_is_in_the_photo_frame
                        )
                    )
                }
            }, onBarcodeError = {
                it.localizedMessage?.let { it1 -> showQRCodeError(getString(R.string.error), it1) }
            })
        }
    }

    private fun showQRCodeError(title: String, message: String) {
        MaterialAlertDialogBuilder(this).setTitle(title).setMessage(message)
            .setPositiveButton(resources.getString(R.string.retry)) { _, _ ->
                startScan()
            }.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun startScan() {
        if (MaghribMengajiPref.readBoolean(
                this, MaghribMengajiPref.ML_KIT_SCANNER_ENABLED_KEY, true
            )
        ) {
            launchScannerUseCase(this, scannerLauncher)
        } else {
            val bottomSheet = ImagePickerBottomSheetDialog().apply {
                onCameraClick = {
                    openCamera()
                }
                onGalleryClick = {
                    openGallery()
                }
            }
            this.let { it1 ->
                bottomSheet.show(
                    it1.supportFragmentManager, ImagePickerBottomSheetDialog.TAG
                )
            }
        }
    }

    private fun openCamera() {
        if (requestPermissionsUseCase.hasCameraPermission(this)) {
            // Proceed with camera access
            launchCameraUseCase(this, cameraLauncher)
        } else {
            showPermissionSettingsDialog()
        }
    }

    private fun openGallery() {
        if (requestPermissionsUseCase.hasReadMediaImagesPermission(this)) {
            // Proceed with gallery access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showModernImagePicker()
            } else {
                launchGalleryUseCase(this, galleryLauncher)
            }
        } else {
            showPermissionSettingsDialog()
        }
    }

    // For Android 14+ (modern photo picker)
    private fun showModernImagePicker() {
        pickMediaModernLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showPermissionRationale(permission: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.allow_notification))
            .setMessage(getString(R.string.you_will_not_receive_a_notification_if_the_permission_is_not_granted))
            .setPositiveButton(resources.getString(R.string.okay)) { _, _ ->
                requestPermissionsUseCase(
                    requestPermissionsLauncher,
                    this,
                    arrayOf(permission)
                )
            }
            .setNegativeButton(resources.getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_disabled))
            .setMessage(getString(R.string.this_permission_is_disabled_please_enable_it_in_the_app_settings))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(resources.getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun signOut() {
        // Sign out and navigate to login

        Firebase.auth.signOut() // Sign out from firebase

        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent the user from coming back to it
    }

    private fun updateMenuItemVisibility() {
        val user = studentRepository.getStudent()
        if (user != null) {
            binding.mainNavigationView.menu.findItem(R.id.menu_balance).isVisible =
                (user.role != MaghribMengajiUser.ROLE_STUDENT)
            binding.mainNavigationView.menu.findItem(R.id.menu_referral_code).isVisible =
                (user.role == MaghribMengajiUser.ROLE_AFFILIATE)

        }
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
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)

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
                        org.greenrobot.eventbus.EventBus.getDefault().post(
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

                        // Update menu UI
                        updateMenuItemVisibility()

                        // Start retrieving user data
                        student.ustadhId?.let { retrieveUstadhProfile(it) }
                        retrieveQuranPageStudent(student.id!!) // Retrieve student's pages
                        retrieveUserTransactions(student.id!!) // Retrieve user's transactions

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
            org.greenrobot.eventbus.EventBus.getDefault().post(
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

    private fun retrieveUserTransactions(uid: String) {
        retrieveUserTransactions(uid) {
            it.onSuccess { transactions ->
                transactionRepository.setRecords(transactions, true)
            }.onFailure { exception ->
                Toast.makeText(
                    this,
                    getString(
                        R.string.error_retrieving_user_transactions,
                        exception.localizedMessage
                    ), Toast.LENGTH_LONG
                ).show()
            }
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

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleDailyNotificationUseCase(this)
            } else {
                cancelDailyNotificationUseCase(this)
            }
        }

        val destinationIds =
            setOf(R.id.volumeListFragment, R.id.pageListFragment, R.id.pageFragment)
        if (navController.currentDestination?.id in destinationIds) {
            binding.mainBottomNavigationView.menu.findItem(R.id.menu_bottom_nav_volume).isChecked =
                true
        } else binding.mainBottomNavigationView.menu.findItem(R.id.menu_bottom_nav_home).isChecked =
            true
    }

    override fun onActivityResult(result: ActivityResult) {
        val resultCode = result.resultCode
        val resultX = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        if (resultCode == Activity.RESULT_OK && resultX != null) {
            val pages = resultX.pages
            if (!pages.isNullOrEmpty()) {

                val bitmap = loadBitmapFromUri(this, pages[0].imageUri)
                // Check if bitmap is not null
                if (bitmap != null) {
                    qrCodeScannerUseCase(bitmap, onBarcodeSuccess = { barcodeResult ->
                        if (barcodeResult != null) {
                            val pageId = extractQRCodeToPageIdUseCase(barcodeResult)

                            // If page id not null and in between 1 and 604, navigate to similarity score fragment
                            if (pageId != null && pageId in 1..604) {
                                // Bundle to pass the data
                                val bundle = Bundle().apply {
                                    putString("imageUriString", pages[0].imageUri.toString())
                                    putInt("pageId", pageId)
                                }

                                // Navigate to the ResultFragment with the Bundle
                                navController.navigate(
                                    R.id.action_global_similarityScoreFragment,
                                    bundle
                                )
                            } else {
                                // If pageId is not found or not match
                                showQRCodeError(
                                    getString(R.string.qr_code_not_detected), getString(
                                        R.string.make_sure_the_qr_code_is_clearly_visible_in_the_photo
                                    )
                                )
                            }
                        } else {
                            // If QR Code not found
                            showQRCodeError(
                                getString(R.string.qr_code_not_detected), getString(
                                    R.string.make_sure_the_qr_code_is_in_the_photo_frame
                                )
                            )
                        }
                    }, onBarcodeError = {
                        it.localizedMessage?.let { it1 ->
                            showQRCodeError(
                                getString(R.string.error),
                                it1
                            )
                        }
                    })
                }
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                this,
                getString(R.string.error_scanner_cancelled),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.error_default_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}