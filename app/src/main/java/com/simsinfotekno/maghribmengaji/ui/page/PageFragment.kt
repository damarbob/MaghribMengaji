package com.simsinfotekno.maghribmengaji.ui.page

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.RecordingActivity
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.ui.ImagePickerBottomSheetDialog
import com.simsinfotekno.maghribmengaji.ui.infaq.InfaqFragment
import com.simsinfotekno.maghribmengaji.usecase.CheckOwnedQuranVolumeUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchCameraUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchGalleryUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.QueryGalleryUseCase
import com.simsinfotekno.maghribmengaji.usecase.RequestPermissionsUseCase
import com.simsinfotekno.maghribmengaji.utils.BitmapToUriUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PageFragment : Fragment(), ActivityResultCallback<ActivityResult> {

    companion object {
        private val TAG = PageFragment::class.java.simpleName
        fun newInstance() = PageFragment()
    }

    private val viewModel: PageViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentPageBinding? = null
    private val binding get() = _binding!!

    // Variables
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bottomSheetBehaviorCheckResult: BottomSheetBehavior<View>
    private var pageImageUrl: String? = null
    private var isPageViewMode: Boolean = true
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                when {
                    !isGranted -> {
                        // Handle the case where the user denied the permission
                        Toast.makeText(
                            requireContext(),
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
    private lateinit var pickMediaModernLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    // Use case
    private val requestPermissionsUseCase = RequestPermissionsUseCase()
    private val launchScannerUseCase = LaunchScannerUseCase()
    private val launchCameraUseCase = LaunchCameraUseCase()
    private val launchGalleryUseCase = LaunchGalleryUseCase()
    private val queryGalleryUseCase = QueryGalleryUseCase()
    private val checkOwnedQuranVolumeUseCase = CheckOwnedQuranVolumeUseCase()
//    private lateinit var networkConnectivityUseCase: NetworkConnectivityUseCase

    private val PICK_IMAGE_REQUEST = 1
    private var pageId: Int? = null
    private var pageStudent: QuranPageStudent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the scanner launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            this
        )
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    val imageBitmap = it.data?.extras?.get("data") as Bitmap
                    val imageUri = BitmapToUriUtil.saveBitmapToFile(requireContext(), imageBitmap)

                    // Submit image to similarity fragment
                    submitImage(imageUri)

                } else if (it.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_scanner_cancelled),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_default_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchCameraUseCase(
                        requireContext(),
                        cameraLauncher,
                    )
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.camera_permission_is_required_to_take_a_photo),
                        Toast.LENGTH_SHORT
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
                        requireContext(),
                        getString(R.string.error_scanner_cancelled),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_default_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        requestGalleryPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    launchGalleryUseCase(
                        requireContext(),
                        galleryLauncher
                    )
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.gallery_permission_is_required_to_select_a_photo),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // For Android 14+ (modern picker)
        pickMediaModernLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                submitImage(it)
            }
        }

        // Set the transition for this fragment
        enterTransition = if (arguments?.getBoolean("previous") == true) {
            MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        } else {
            MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBinding.inflate(inflater, container, false)

        // Check connection
//        networkConnectivityUseCase = NetworkConnectivityUseCase(requireContext())
//        checkConnection()

        pageId = arguments?.getInt("pageId")
        viewModel.pageId = pageId

        val page = quranPageRepository.getRecordById(pageId) // Get QuranPage instance
        val volume = quranVolumeRepository.getRecordByPageId(page!!.id) // Get QuranVolume instance
        pageStudent =
            quranPageStudentRepository.getRecordByPageId(pageId) // Get student's page instance if any
        val ownedVolume = MainApplication.studentRepository.getStudent()?.ownedVolumeId

        viewModel.setQuranPageStudent(pageStudent) // Set QuranPageStudent to viewModel

        /* Check owned volume ID */
        checkOwnedQuranVolumeUseCase(ownedVolume,
            pageId = pageId,
            onNotOwnedVolume = {
                notOwnedVolume()
            })

        /* Infaq on first volume open */
        if (volume != null) {
            if (!MaghribMengajiPref.readBoolean(requireActivity(), "${MaghribMengajiPref.QURAN_VOLUME_INFAQ}_${volume.id}", false)) {
                InfaqFragment().show(parentFragmentManager, InfaqFragment::class.java.simpleName)
                MaghribMengajiPref.saveBoolean(requireActivity(), "${MaghribMengajiPref.QURAN_VOLUME_INFAQ}_${volume.id}", true)
            }
        }

        /* Views */
        binding.pageCollapsingToolbarLayout.title =
            getString(R.string.page_x, pageId.toString())
        binding.pageTextViewVolume.text = getString(R.string.volume_x, volume?.id.toString())
        binding.pageImageViewPage.visibility = View.GONE
        binding.pageCheckResultHolder.visibility = View.GONE

        // Set background image height
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val params = binding.pageImageViewPage.layoutParams
        params.height =
            displayMetrics.heightPixels - resources.getDimension(com.google.android.material.R.dimen.abc_action_bar_default_height_material)
                .toInt() - 176
        binding.pageImageViewPage.layoutParams = params


        if (pageStudent != null) viewModel.setToResultViewMode() else viewModel.setToPageViewMode()

        bottomSheetBehaviorCheckResult =
            BottomSheetBehavior.from(binding.pageBottomSheetCheckResult.bottomSheetCheckResult)

        // Initial state of check result bottom sheet
        bottomSheetBehaviorCheckResult.state = BottomSheetBehavior.STATE_HIDDEN

        // Callback on state change to show button check result
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Do something for new state.
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded) {
                            val materialFade = MaterialFade().apply {
                                duration = 150L
                            }
                            container?.let {
                                TransitionManager.beginDelayedTransition(it, materialFade)
                            }
                            binding.pageButtonCheckResult.visibility = View.VISIBLE
                        }
                    }, 250)
                    container?.let {
                        TransitionManager.endTransitions(it)
                    }
                    binding.pageLinearLayout.isNestedScrollingEnabled = true
                    binding.pageCollapsingToolbarLayout.isNestedScrollingEnabled = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.

            }
        }

        // To add the callback:
        bottomSheetBehaviorCheckResult.addBottomSheetCallback(bottomSheetCallback)

        // Back press when bottom sheet showed
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bottomSheetBehaviorCheckResult.state != BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetBehaviorCheckResult.state = BottomSheetBehavior.STATE_HIDDEN
                        binding.pageLinearLayout.isNestedScrollingEnabled = true
                        binding.pageCollapsingToolbarLayout.isNestedScrollingEnabled = true
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        bottomSheetBehaviorCheckResult.removeBottomSheetCallback(bottomSheetCallback)
                    }
                }
            })

        binding.pageButtonPrevious.isEnabled = pageId != 1
        binding.pageButtonForward.isEnabled = pageId != 604

        if (pageStudent == null) {
            // If student's page is not found or student had not submitted

            binding.pageButtonSubmit.visibility = View.VISIBLE // Show submit button
            binding.pageButtonCheckResult.visibility = View.GONE // Hide check result button
        } else {
            // If student had submitted

            binding.pageButtonSubmit.visibility = View.GONE // Show submit button
            binding.pageButtonCheckResult.visibility = View.VISIBLE // Show check result button
        }

//        binding.pageBottomSheetCheckResult.apply {
//            val ocrScore = pageStudent?.oCRScore ?: 0
//            val tidinessScore = pageStudent?.tidinessScore ?: 0
//            val accuracyScore = pageStudent?.accuracyScore ?: 0
//            val consistencyScore = pageStudent?.consistencyScore ?: 0
//
//            val overallScore = (ocrScore + tidinessScore + accuracyScore + consistencyScore) / 4
//
//            this.checkResultTextViewOverallScore.text = overallScore.toString()
//            this.checkResultTextViewPreliminaryResult.text = ocrScore.toString()
//            this.checkResultTextViewTidinessResult.text = tidinessScore.toString()
//            this.checkResultTextViewAccuracyResult.text = accuracyScore.toString()
//            this.checkResultTextViewConsistencyResult.text = consistencyScore.toString()
//
//            this.checkResultCircularProgressScore.progress = overallScore
//            this.checkResultProgressIndicatorPreliminary.progress = ocrScore
//            this.checkResultProgressIndicatorTidiness.progress = tidinessScore
//            this.checkResultProgressIndicatorAccuracy.progress = accuracyScore
//            this.checkResultProgressIndicatorConsistency.progress = consistencyScore
//
//            pageStudent?.pictureUriString?.let {
//                loadImageIntoImageView(it, this.checkResultImageViewStudentPageImage, true)
//                this.checkResultLinearProgress.visibility = View.GONE
//            }
//        }
//        viewModel.setToPageViewMode()
//        setCheckResult()

        /* Observers */
        // Observe QuranPageStudent
        viewModel.quranPageStudent.observe(viewLifecycleOwner) { quranPageStudent ->
            Log.d(TAG, "ViewModel's QuranPageStudent: $quranPageStudent")
            val ocrScore = quranPageStudent?.oCRScore ?: 0
            val tidinessScore = quranPageStudent?.tidinessScore ?: 0
            val accuracyScore = quranPageStudent?.accuracyScore ?: 0
            val consistencyScore = quranPageStudent?.consistencyScore ?: 0

            binding.pageCheckResultTextViewOverallScore.text = ((ocrScore + tidinessScore + accuracyScore + consistencyScore) / 4).toString()
            binding.pageCheckResultCircularProgressScore.progress = (ocrScore + tidinessScore + accuracyScore + consistencyScore) / 4

            binding.pageCheckResultTextViewPreliminaryResult.text = ocrScore.toString()
            binding.pageCheckResultProgressIndicatorPreliminary.progress = ocrScore

            binding.pageCheckResultTextViewTidinessResult.text = tidinessScore.toString()
            binding.pageCheckResultProgressIndicatorTidiness.progress = tidinessScore

            binding.pageCheckResultTextViewAccuracyResult.text = accuracyScore.toString()
            binding.pageCheckResultProgressIndicatorAccuracy.progress = accuracyScore

            binding.pageCheckResultTextViewConsistencyResult.text = consistencyScore.toString()
            binding.pageCheckResultProgressIndicatorConsistency.progress = consistencyScore

            if (tidinessScore != 0 && accuracyScore != 0 && consistencyScore != 0) {
                binding.pageCheckResultTextViewStatus.visibility = View.GONE
            } else {
                binding.pageCheckResultTextViewStatus.visibility = View.VISIBLE
            }

//            binding.pageCheckResultTextViewUploadedAt.text =
//                getString(R.string.submitted_at_x, Calendar.getInstance().apply {
//                    timeInMillis = quranPageStudent?.createdAt!!.seconds*1000
//                }.time)
            val sfd = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.DEFAULT, Locale.getDefault())
            binding.pageCheckResultTextViewUploadedAt.text =
                getString(R.string.submitted_at_x,
//                    quranPageStudent?.createdAt?.seconds?.let { sfd.format(Date(it*1000)) })
                    quranPageStudent?.createdAt?.toDate()?.let { sfd.format(it) })

            quranPageStudent?.pictureUriString?.let {
                loadImageIntoImageView(it, binding.pageCheckResultImageViewStudentPageImage, false)
                binding.pageCheckResultLinearProgress.visibility = View.GONE
            }
        }
        mainViewModel.connectionStatus.observe(viewLifecycleOwner) {
            binding.pageButtonSubmit.isEnabled = it == ConnectivityObserver.Status.Available
        }
        viewModel.pageViewMode.observe(viewLifecycleOwner) {
            isPageViewMode = it
            Log.d(TAG, "view mode $it")
            Log.d(TAG, "$pageStudent")
            if (pageStudent != null) {
                if (it) showPage(container)
                else showResult(container)
            } else showPageNoPageStudent(container)
        }

        /* Listeners */
        binding.pageToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.pageAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                // Collapsed
//                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
//                    true
//                )
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
//                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
//                    false
//                )
            }
        }
        binding.pageButtonForward.setOnClickListener {
            val newPageId = pageId!! + 1
            val bundle = Bundle()
            bundle.putInt("pageId", newPageId)
            if (isAdded) {
                findNavController().navigate(R.id.action_global_pageFragment, bundle)
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            }
        }

        binding.pageButtonPrevious.setOnClickListener {
            val newPageId = pageId!! - 1
            val bundle = Bundle().apply {
                putInt("pageId", newPageId)
                putBoolean("previous", true)
            }
            if (isAdded) {
                findNavController().navigate(R.id.action_global_pageFragment, bundle)
            }
        }

        binding.pageButtonSubmit.isEnabled = true
        // Submit button
        binding.pageButtonSubmit.setOnClickListener {
            if (isAdded) {
//                if (Build.VERSION.SDK_INT >= 30) {
                if (MaghribMengajiPref.readBoolean(
                        requireActivity(),
                        MaghribMengajiPref.ML_KIT_SCANNER_ENABLED_KEY,
                        true
                    )
                ) {
                    binding.pageButtonSubmit.isEnabled = false
                    launchScannerUseCase(requireActivity(), scannerLauncher)
                } else {
                    val bottomSheet = ImagePickerBottomSheetDialog().apply {
                        onCameraClick = {
                            openCamera()
                        }
                        onGalleryClick = {
                            openGallery()
                        }
                    }
                    activity?.let { it1 ->
                        bottomSheet.show(
                            it1.supportFragmentManager,
                            ImagePickerBottomSheetDialog.TAG
                        )
                    }
                }
            }
        }

        binding.pageButtonRecite.setOnClickListener {
            if (isAdded) {
                val intent = Intent(this.context, RecordingActivity::class.java).apply {
                    putExtra("pageId", pageId)
                }
                startActivity(intent)
            }
        }

        binding.pageButtonCheckResult.setOnClickListener {
            if (isAdded) {
                viewModel.setToResultViewMode() // Set to result view mode on ViewMode
                showResult(container)
            }
        }
        binding.pageButtonCheckPage.setOnClickListener {
            if (isAdded) {
                viewModel.setToPageViewMode() // Set to page view mode on ViewMode
                showPage(container)
            }
        }

        return binding.root
    }

    private fun showPermissionRationale(permission: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_required))
            .setMessage(
                getString(
                    R.string.this_feature_requires_the_permission_please_grant_it,
                    permission
                ))
            .setPositiveButton(resources.getString(R.string.okay)) { _, _ ->
                requestPermissionsUseCase(
                    requestPermissionsLauncher,
                    requireContext(),
                    arrayOf(permission)
                )
            }
            .setNegativeButton(resources.getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
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
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun openCamera() {
        if (requestPermissionsUseCase.hasCameraPermission(requireContext())) {
            // Proceed with camera access
            launchCameraUseCase(requireContext(), cameraLauncher)
            Log.d(TAG, "camera granted")
        } else {
            showPermissionSettingsDialog()
        }
    }

    private fun openGallery() {
        if (requestPermissionsUseCase.hasReadMediaImagesPermission(requireContext())) {
            // Proceed with gallery access
//            launchGalleryUseCase(requireContext(), galleryLauncher)
//            lifecycleScope.launch {
//                queryGalleryUseCase(requireContext().contentResolver)
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showModernImagePicker()
            } else {
                launchGalleryUseCase(requireContext(), galleryLauncher)
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

    private fun notOwnedVolume() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.afwan))
            .setMessage(resources.getString(R.string.this_volume_is_locked))
            .setPositiveButton(resources.getString(R.string.okay)) { dialog, _ ->
                findNavController().popBackStack()
                dialog.dismiss()  // Dismiss the dialog when OK is clicked
            }
            .setCancelable(false)
            .show()
    }

    private fun showPage(container: ViewGroup?) {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
//                container?.let { TransitionManager.beginDelayedTransition(it, materialFade) }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
//        loadImageIntoImageView(pageImageUrl, binding.pageImageViewPage, isBottomSheet = false)
        binding.pageButtonCheckResult.visibility = View.VISIBLE
        binding.pageButtonCheckPage.visibility = View.GONE
        binding.pageImageViewPage.visibility = View.VISIBLE
        binding.pageCheckResultHolder.visibility = View.GONE
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (isAdded) {
////                        bottomSheetBehaviorCheckResult.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }, 100)
        TransitionManager.endTransitions(container)
    }

    private fun showPageNoPageStudent(container: ViewGroup?) {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
//        loadImageIntoImageView(pageImageUrl, binding.pageImageViewPage, isBottomSheet = false)
        binding.pageButtonSubmit.visibility = View.VISIBLE
        binding.pageButtonCheckResult.visibility = View.GONE
        binding.pageButtonCheckPage.visibility = View.GONE
        binding.pageImageViewPage.visibility = View.VISIBLE
        binding.pageCheckResultHolder.visibility = View.GONE
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (isAdded) {
////                        bottomSheetBehaviorCheckResult.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }, 100)
        TransitionManager.endTransitions(container)
    }

    private fun showResult(container: ViewGroup?) {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
//                container?.let { TransitionManager.beginDelayedTransition(it, materialFade) }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
        binding.pageButtonCheckResult.visibility = View.GONE
        binding.pageButtonCheckPage.visibility = View.GONE
        binding.pageImageViewPage.visibility = View.GONE
        binding.pageCheckResultHolder.visibility = View.VISIBLE
        binding.pageProgressBar.visibility = View.GONE
//        loadImageIntoImageView(
//            pageStudent?.pictureUriString,
//            binding.pageCheckResultImageViewStudentPageImage,
//            false
//        )
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (isAdded) {
////                        bottomSheetBehaviorCheckResult.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }, 100)
        binding.pageLinearLayout.isNestedScrollingEnabled = false
        binding.pageCollapsingToolbarLayout.isNestedScrollingEnabled = false
        TransitionManager.endTransitions(container)
    }

//    private fun checkConnection() {
//        networkConnectivityUseCase(viewLifecycleOwner, onAvailableNetwork = {
//            binding.pageButtonSubmit.isEnabled = true
//        }, onUnavailableNetwork = {
//            binding.pageButtonSubmit.isEnabled = false
//        })
//    }

    private fun setCheckResult() {
        val ocrScore = pageStudent?.oCRScore ?: 0
        val tidinessScore = pageStudent?.tidinessScore ?: 0
        val accuracyScore = pageStudent?.accuracyScore ?: 0
        val consistencyScore = pageStudent?.consistencyScore ?: 0

        binding.pageCheckResultTextViewOverallScore.text = ocrScore.toString()
        binding.pageCheckResultTextViewPreliminaryResult.text = ocrScore.toString()
        binding.pageCheckResultTextViewTidinessResult.text = tidinessScore.toString()
        binding.pageCheckResultTextViewAccuracyResult.text = accuracyScore.toString()
        binding.pageCheckResultTextViewConsistencyResult.text = consistencyScore.toString()

        binding.pageCheckResultCircularProgressScore.progress = ocrScore
        binding.pageCheckResultProgressIndicatorPreliminary.progress = ocrScore
        binding.pageCheckResultProgressIndicatorTidiness.progress = tidinessScore
        binding.pageCheckResultProgressIndicatorAccuracy.progress = accuracyScore
        binding.pageCheckResultProgressIndicatorConsistency.progress = consistencyScore

        pageStudent?.pictureUriString?.let {
            loadImageIntoImageView(it, binding.pageCheckResultImageViewStudentPageImage, false)
            binding.pageCheckResultLinearProgress.visibility = View.GONE
        }
    }

    // Load image to imageview
    private fun loadImageIntoImageView(
        imageUrl: String? = null,
        imageView: ImageView,
        isBottomSheet: Boolean = false // Check position of imageView
    ) {
        Log.d("TAG", imageUrl.toString())
//        val progress =
//            if (isBottomSheet) binding.pageBottomSheetCheckResult.checkResultLinearProgress else binding.pageProgressBar
        binding.pageProgressBar.visibility = View.VISIBLE
//        progress.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (isAdded) {
                        binding.pageProgressBar.visibility = View.GONE
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//                        progress.visibility = View.GONE
//                        imageView.visibility = View.VISIBLE
                    }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    if (isAdded) {
                        binding.pageProgressBar.visibility = View.GONE
//                        progress.visibility = View.GONE
//                        imageView.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.failed_to_load_image) + e,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.d(TAG, e.toString())

                    }
                    return false
                }
            })
            .error(R.mipmap.vector_maghrib_mengaji)
            .fallback(R.mipmap.vector_maghrib_mengaji)
            .into(imageView)
    }


    private fun submitImage(imageUri: Uri?) {
        // Bundle to pass the data
        val bundle = Bundle().apply {
            putString(
                "imageUriString",
                imageUri
                    .toString()
            )
            putInt("pageId", pageId!!)
        }

        // Navigate to the ResultFragment with the Bundle
        findNavController().navigate(
            R.id.action_pageFragment_to_similarityScoreFragment,
            bundle
        )
    }

    // Get image file
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            val fileUri = data?.data
//
//            // Upload image to Firebase storage TODO: Move to use case
//            fileUri?.let {
//                uploadImageUseCase(pageId!!, it, {
//                    Toast.makeText(
//                        requireContext(),
//                        "Image uploaded successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }, { exception ->
//                    Toast.makeText(
//                        requireContext(),
//                        "Failed to upload image: ${exception.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                })
//            } ?: Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
//
//        }
//    }

    override fun onActivityResult(result: ActivityResult) {
        val resultCode = result.resultCode
        val resultX = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        if (resultCode == Activity.RESULT_OK && resultX != null) {
            val pages = resultX.pages
            if (!pages.isNullOrEmpty()) {

                // Bundle to pass the data
                val bundle = Bundle().apply {
                    putString("imageUriString", pages[0].imageUri.toString())
                    putInt("pageId", pageId!!)
                }

                // Navigate to the ResultFragment with the Bundle
                findNavController().navigate(
                    R.id.action_pageFragment_to_similarityScoreFragment,
                    bundle
                )

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_scanner_cancelled),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_default_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.pageButtonSubmit.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}