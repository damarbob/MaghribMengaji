package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiPref
import com.simsinfotekno.maghribmengaji.ui.ImagePickerBottomSheetDialog
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.ExtractQRCodeToPageIdUseCase
import com.simsinfotekno.maghribmengaji.usecase.EditDistanceSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromOCRApiJSON
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromQuranAPIJSON
import com.simsinfotekno.maghribmengaji.usecase.FetchQuranPageUseCase
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.LaunchCameraUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchGalleryUseCase
import com.simsinfotekno.maghribmengaji.usecase.LaunchScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.LoadBitmapFromUri
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import com.simsinfotekno.maghribmengaji.usecase.QRCodeScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.RequestPermissionsUseCase
import com.simsinfotekno.maghribmengaji.utils.BitmapToUriUtil
import kotlinx.coroutines.CompletableDeferred

// TODO: set timeout, fix when no connection on upload page (maybe page document created but image not uploaded? however it fixed on relaunch app)
class SimilarityScoreFragment : Fragment(),
//    FetchQuranPageUseCase.ResultHandler,
//    OCRAsyncTask.IOCRCallBack,
    ActivityResultCallback<ActivityResult> {

    companion object {
        private val TAG = SimilarityScoreFragment::class.java.simpleName
        fun newInstance() = SimilarityScoreFragment()
    }

    /* ViewModels */
    private val viewModel: SimilarityScoreViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentSimilarityScoreBinding? = null
    private val binding get() = _binding!!

    // OCR and Quran API
    private var quranApiResult: String? = null
    private var ocrResult: String? = null
    private val quranApiResultDeferred = CompletableDeferred<String>()

    /* Variables */
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bottomSheetBehaviorCheckResult: BottomSheetBehavior<View>
    private var pageId: Int? = null
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: Uri
    private lateinit var container: ViewGroup
    private var similarityIndex: Int = 0
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
//    private lateinit var cameraModernLauncher: ActivityResultLauncher<Camera>

    /* Use cases */
    private val requestPermissionsUseCase = RequestPermissionsUseCase()
    private val loadBitmapFromUri = LoadBitmapFromUri()
    private val oCRAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase()
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val editDistanceSimilarityIndex= EditDistanceSimilarityIndex()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()
    private val bitmapToBase64 = BitmapToBase64()
    private val launchScannerUseCase = LaunchScannerUseCase()
    private val launchCameraUseCase = LaunchCameraUseCase()
    private val launchGalleryUseCase = LaunchGalleryUseCase()

    // TODO: KKM decided by ustadh
    private val kkm = 60

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var materialAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the scanner launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(), this
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
                        context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT
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
                        requireContext(), galleryLauncher
                    )
                } else {
                    Toast.makeText(
                        context, "Camera permission is required to take a photo", Toast.LENGTH_SHORT
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

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        materialAlertDialog = MaterialAlertDialogBuilder(requireContext()).create()

        // Set up the back button callback
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (quranPageStudentRepository.getRecordByPageId(viewModel.getPageId()) == null) {
                    materialAlertDialog =
                        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                            .setMessage(getString(R.string.your_result_will_be_gone))
                            .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                                dialog.dismiss()
                            }.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                                findNavController().popBackStack()
                            }.show()
                } else findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSimilarityScoreBinding.inflate(inflater, container, false)

        this.container = container!!

//        binding.similarityScoreTextViewPage.text =
//            resources.getString(R.string.page_x, arguments?.getInt("pageId").toString())

        /* Observers */

        viewModel.pageId.observe(viewLifecycleOwner) {
            binding.similarityScoreTextViewPage.text =
                resources.getString(R.string.page_x, it.toString())
        }

        // Listening to remote db result whether success or failed
        viewModel.remoteDbResult.observe(viewLifecycleOwner) { uploadResult ->
            uploadResult?.onSuccess {

                // Navigate to the next screen or update UI
                Toast.makeText(
                    requireContext(), getString(R.string.upload_successful), Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack() // Back to page

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                binding.similarityScoreButtonUpload.isEnabled = true
                binding.similarityScoreButtonUploadLow.isEnabled = true
                binding.similarityScoreButtonRetry.isEnabled = true
                binding.similarityScoreButtonRetryLow.isEnabled = true
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        viewModel.totalScore.observe(viewLifecycleOwner) { totalScore ->
            if (totalScore != null) {
                // Maximize view and show score if similarityIndex is already defined
//                maximizeView(maximized = true, scorePassed = it > kkm, onCreateView = false)
//                binding.similarityScoreTextViewScore.text = it.toString()
//                binding.similarityScoreCircularProgressScore.progress = it.toInt()
//                binding.similarityScoreCircularProgress.visibility = View.GONE

                // Maximize view and show score
                if (totalScore > kkm) {
                    binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                        getString(R.string.your_score_is_great_press_upload_to_send_your_score),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    binding.similarityScoreCircularProgress.visibility = View.GONE
                    maximizeView(maximized = true, scorePassed = true, onCreateView = false)
                } else {
                    binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                        getString(R.string.your_score_is_low), HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    binding.similarityScoreCircularProgress.visibility = View.GONE
                    maximizeView(maximized = true, scorePassed = false, onCreateView = false)
                }

                // Add animation
                ValueAnimator.ofInt(totalScore.toInt()).apply {
                    duration = 1000
                    addUpdateListener {
                        val animationValue = it.animatedValue as Int
                        binding.similarityScoreTextViewScore.text = animationValue.toString()
                        binding.similarityScoreCircularProgressScore.progress = animationValue
                    }
                }.start()
            }
//            else maximizeView(maximized = false, scorePassed = false, onCreateView = false)
        }
        viewModel.maghribBonus.observe(viewLifecycleOwner) { maghribBonus ->
            binding.similarityScoreTextViewMaghribBonus.text = "+$maghribBonus"
        }
        viewModel.submitStreakBonus.observe(viewLifecycleOwner) { submitStreakBonus ->
            binding.similarityScoreTextViewStreak.text = getString(
                R.string.submit_streak_bonus_x_day_s, submitStreakBonus[0].toInt().toString()
            )
            binding.similarityScoreTextViewStreakBonus.text =
                "+${((submitStreakBonus[1] - 1) * 100).toInt()}%"
        }
        viewModel.similarityScore.observe(viewLifecycleOwner) { similarityScore ->
            binding.similarityScoreTextViewInitialScore.text = similarityScore.toString()
        }

        // Get image uri from view model if any, if not, get from arguments
        // Check if similarityIndex is already defined in ViewModel
//        viewModel.similarityIndex.observe(viewLifecycleOwner) { index ->
//            index?.let {
//                // Maximize view and show score if similarityIndex is already defined
//                maximizeView(maximized = true, scorePassed = it > kkm, onCreateView = false)
//                binding.similarityScoreTextViewScore.text = it.toString()
//                binding.similarityScoreCircularProgressScore.progress = it
//                binding.similarityScoreCircularProgress.visibility = View.GONE
//                similarityIndex = it
//            }
//        }
        viewModel.progressVisibility.observe(viewLifecycleOwner) { isVisible ->
            Log.d(TAG, "Progress visibility: $isVisible")
            binding.similarityScoreCircularProgress.visibility =
                if (isVisible) View.VISIBLE else View.GONE
            binding.similarityScoreCircularProgressScore.visibility =
                if (isVisible) View.INVISIBLE else View.VISIBLE
        }
        mainViewModel.connectionStatus.observe(viewLifecycleOwner) {
            if (it != ConnectivityObserver.Status.Available) {
                binding.similarityScoreButtonUpload.isEnabled = false
                binding.similarityScoreButtonUploadLow.isEnabled = false
                binding.similarityScoreButtonRetry.isEnabled = false
                binding.similarityScoreButtonRetryLow.isEnabled = false
            } else {
                binding.similarityScoreButtonUpload.isEnabled = true
                binding.similarityScoreButtonUploadLow.isEnabled = true
                binding.similarityScoreButtonRetry.isEnabled = true
                binding.similarityScoreButtonRetryLow.isEnabled = true
            }
        }

        /* Variables and arguments */
        if (viewModel.imageUriString == null) {
            viewModel.imageUriString = arguments?.getString("imageUriString")
        }
        if (viewModel.getPageId() == null) {
            viewModel.setPageId(arguments?.getInt("pageId"))
        }
        if (viewModel.bitmap == null) {
            viewModel.bitmap = loadBitmapFromUri(
                requireContext(), Uri.parse(arguments?.getString("imageUriString"))
            )
        }

        startScoring()

        /* View */
//        maximizeView(
//            maximized = false,
//            scorePassed = false,
//            onCreateView = true
//        ) // Show only progress indicator and close button

        /* Listeners */
        binding.similarityScoreButtonUpload.setOnClickListener {
            materialAlertDialog =
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                    .setMessage(
                        getString(
                            R.string.your_similarity_score, viewModel.totalScore.value.toString()
                        )
                    ).setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        uploadPage()
                    }.show()
        }

        binding.similarityScoreButtonUploadLow.setOnClickListener {
            materialAlertDialog =
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                    .setMessage(
                        getString(
                            R.string.your_similarity_score, viewModel.totalScore.value.toString()
                        )
                    ).setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        uploadPage()
                    }.show()
        }

        binding.similarityScoreButtonRetry.setOnClickListener {
            materialAlertDialog =
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.yes)) { _, _ ->
//                    if (Build.VERSION.SDK_INT >= 30) {
                        retryScan()
                    }.show()
        }

        binding.similarityScoreButtonRetryLow.setOnClickListener {
            materialAlertDialog =
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        retryScan()
                    }.show()
        }

        binding.similarityScoreButtonClose.setOnClickListener {
//            if (binding.similarityScoreButtonUpload.visibility == View.GONE || binding.similarityScoreButtonUploadLow.visibility == View.GONE || binding.similarityScoreCircularProgress.visibility == View.VISIBLE) {
//            if (binding.similarityScoreTextViewScore.visibility == View.GONE || binding.similarityScoreCircularProgress.visibility == View.VISIBLE) {
//                backPressedCallback.handleOnBackPressed()
//            } else findNavController().popBackStack()
            if (quranPageStudentRepository.getRecordByPageId(viewModel.getPageId()) == null) {
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.your_result_will_be_gone))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.yes)) { _, _ ->
                        findNavController().popBackStack()
                    }.show()
            } else findNavController().popBackStack()
        }

        return binding.root
    }

    private fun startScoring() {
        val pageStudent = quranPageStudentRepository.getRecordByPageId(viewModel.getPageId())

        if (pageStudent != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.you_have_submitted_this_page),
                Toast.LENGTH_LONG
            ).show()
            binding.similarityScoreTextViewScore.visibility = View.VISIBLE
            binding.similarityScoreCircularProgressScore.visibility = View.VISIBLE
            binding.similarityScoreCircularProgress.visibility = View.GONE
            binding.similarityScoreButtonRetry.isEnabled = false
            binding.similarityScoreButtonRetryLow.isEnabled = false
            binding.similarityScoreButtonUpload.isEnabled = false
            binding.similarityScoreButtonUploadLow.isEnabled = false
            binding.similarityScoreTextViewPage.text =
                getString(R.string.page_have_been_submitted, viewModel.getPageId().toString())
            binding.similarityScoreTextViewPage.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_primary
                )
            )
            binding.similarityScoreTextViewPage.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_onPrimary
                )
            )
            // Add animation
            pageStudent.oCRScore?.let {
                ValueAnimator.ofInt(it).apply {
                    duration = 1000
                    addUpdateListener {
                        val animationValue = it.animatedValue as Int
                        binding.similarityScoreTextViewScore.text = animationValue.toString()
                        binding.similarityScoreCircularProgressScore.progress = animationValue
                    }
                }.start()
            }
            return
        }

        if (MaghribMengajiPref.readBoolean(
                requireActivity(), MaghribMengajiPref.QR_CODE_ENABLED_KEY, true
            )
        ) {
            viewModel.checkQRCode(onSuccess = {
                viewModel.processOCR("ara", lifecycleScope)
            }, onError = { error ->
                Log.d(TAG, "Barcode Error: $error")

                when {
                    error == QRCodeScannerUseCase.QR_CODE_NOT_FOUND -> showQRCodeError(
                        getString(R.string.qr_code_not_detected), getString(
                            R.string.make_sure_the_qr_code_is_in_the_photo_frame
                        )
                    )

                    error == ExtractQRCodeToPageIdUseCase.PAGE_ID_NOT_FOUND -> showQRCodeError(
                        getString(R.string.qr_code_not_detected), getString(
                            R.string.make_sure_the_qr_code_is_clearly_visible_in_the_photo
                        )
                    )

//                    error as Int >= 1 && error <= 604 -> {
                    error as Int in 1..604 -> {
                        Log.d(TAG, "error: $error")
                        showQRCodeError(
                            getString(R.string.the_page_does_not_match), getString(
                                R.string.the_page_you_selected_is_but_the_page_detected_is,
                                viewModel.getPageId().toString(),
                                error
                            ), error
                        )
                    }

                    else -> showQRCodeError(
                        error.toString(),
                        getString(R.string.make_sure_the_qr_code_is_in_the_photo_frame)
                    )
                }
            })
        } else viewModel.processOCR("ara", lifecycleScope)
    }

    private fun uploadPage() {
        viewModel.uploadPageStudent()
        viewModel.updateSubmitStreak()
        binding.similarityScoreButtonUpload.isEnabled = false
        binding.similarityScoreButtonUploadLow.isEnabled = false
        binding.similarityScoreButtonRetry.isEnabled = false
        binding.similarityScoreButtonRetryLow.isEnabled = false
    }

    private fun retryScan() {
        if (MaghribMengajiPref.readBoolean(
                requireActivity(), MaghribMengajiPref.ML_KIT_SCANNER_ENABLED_KEY, true
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
            activity?.let { it1 ->
                bottomSheet.show(
                    it1.supportFragmentManager, ImagePickerBottomSheetDialog.TAG
                )
            }
        }
    }

    private fun showQRCodeError(title: String, message: String, pageId: Int = 0) {
        // Get failure counter
        val failureCounter = MaghribMengajiPref.readInt(
            requireActivity(), MaghribMengajiPref.QR_CODE_FAILURE_COUNTER, 0
        )
        Log.d(TAG, "Failure counter: $failureCounter")
        Log.d(TAG, "pageId: $pageId")

        // If failure counter not more than 3x and no detected page ID
        if (failureCounter < 3 && pageId == 0) {
            MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(message)
                .setPositiveButton(resources.getString(R.string.retry)) { _, _ ->
                    MaghribMengajiPref.saveInt(
                        requireActivity(),
                        MaghribMengajiPref.QR_CODE_FAILURE_COUNTER,
                        failureCounter + 1
                    )
                    retryScan()
                }.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                    MaghribMengajiPref.saveInt(
                        requireActivity(),
                        MaghribMengajiPref.QR_CODE_FAILURE_COUNTER,
                        failureCounter + 1
                    )
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.turn_off_qr_code_check)) { _, _ ->
                    MaghribMengajiPref.saveBoolean(
                        requireActivity(), MaghribMengajiPref.QR_CODE_ENABLED_KEY, false
                    )
                    MaghribMengajiPref.saveInt(
                        requireActivity(), MaghribMengajiPref.QR_CODE_FAILURE_COUNTER, 0
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.qr_code_check_successfully_disabled),
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else if (failureCounter < 3 && pageId > 0 && pageId < 605) {
            Log.d(TAG, "pageId: $pageId true")
            MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(message)
                .setPositiveButton(getString(R.string.go_to, pageId.toString())) { _, _ ->
                    MaghribMengajiPref.saveInt(
                        requireActivity(),
                        MaghribMengajiPref.QR_CODE_FAILURE_COUNTER,
                        failureCounter + 1
                    )
                    viewModel.setPageId(pageId)
                    startScoring()
                }.setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                    MaghribMengajiPref.saveInt(
                        requireActivity(),
                        MaghribMengajiPref.QR_CODE_FAILURE_COUNTER,
                        failureCounter + 1
                    )
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.turn_off_qr_code_check)) { _, _ ->
                    MaghribMengajiPref.saveBoolean(
                        requireActivity(), MaghribMengajiPref.QR_CODE_ENABLED_KEY, false
                    )
                    MaghribMengajiPref.saveInt(
                        requireActivity(), MaghribMengajiPref.QR_CODE_FAILURE_COUNTER, 0
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.qr_code_check_successfully_disabled),
                        Toast.LENGTH_LONG
                    ).show()
                }.show()

        } else if (failureCounter >= 3) {
            MaterialAlertDialogBuilder(requireContext()).setTitle(title)
                .setMessage(getString(R.string.the_qr_code_has_not_been_detected_3_times_or_more_do_you_want_to_turn_off_qr_code_checking))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    MaghribMengajiPref.saveBoolean(
                        requireActivity(), MaghribMengajiPref.QR_CODE_ENABLED_KEY, false
                    )
                    MaghribMengajiPref.saveInt(
                        requireActivity(), MaghribMengajiPref.QR_CODE_FAILURE_COUNTER, 0
                    )
                    startScoring()
                }.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun showPermissionRationale(permission: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.allow_notification))
            .setMessage(getString(R.string.you_will_not_receive_a_notification_if_the_permission_is_not_granted))
            .setPositiveButton(resources.getString(R.string.okay)) { _, _ ->
                requestPermissionsUseCase(
                    requestPermissionsLauncher, requireContext(), arrayOf(permission)
                )
            }.setNegativeButton(resources.getString(R.string.cancel), null).create().show()
    }

    private fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.permission_disabled))
            .setMessage(getString(R.string.this_permission_is_disabled_please_enable_it_in_the_app_settings))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                openAppSettings()
            }.setNegativeButton(resources.getString(R.string.cancel), null).create().show()
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
        } else {
            showPermissionSettingsDialog()
        }
    }

    private fun openGallery() {
        if (requestPermissionsUseCase.hasReadMediaImagesPermission(requireContext())) {
            // Proceed with gallery access
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

    private fun maximizeView(maximized: Boolean, scorePassed: Boolean, onCreateView: Boolean) {
        Log.d(TAG, "similarityScore: ${viewModel.similarityScore.value}")
        if (!onCreateView) { // When onCreateView not animated
            val materialFade = MaterialFade().apply {
                duration = 150L
            }
            TransitionManager.beginDelayedTransition(container, materialFade)
        }
        // Whether to show all or only progress indicator and close button
//        binding.similarityScoreTextView.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewScore.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreButtonUpload.visibility =
//            if (maximized && scorePassed) View.VISIBLE else View.GONE
            if (scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonRetry.visibility =
//            if (maximized && scorePassed) View.VISIBLE else View.GONE
            if (scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewDetail.visibility =
//            if (maximized && scorePassed) View.VISIBLE else View.GONE
            if (scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonUploadLow.visibility =
//            if (maximized && !scorePassed) View.VISIBLE else View.GONE
            if (!scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonRetryLow.visibility =
//            if (maximized && !scorePassed) View.VISIBLE else View.GONE
            if (!scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewDetailLow.visibility =
//            if (maximized && !scorePassed) View.VISIBLE else View.GONE
            if (!scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreCircularProgressScore.visibility =
            if (maximized) View.VISIBLE else View.INVISIBLE
        binding.similarityScoreLinearLayoutDetail.visibility =
            if (maximized) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        materialAlertDialog?.dismiss()
        _binding = null
//        quranApiResultDeferred.cancel()
    }

    override fun onActivityResult(result: ActivityResult) {
        val resultCode = result.resultCode
        val resultX = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        if (resultCode == Activity.RESULT_OK && resultX != null) {
            val pages = resultX.pages
            if (!pages.isNullOrEmpty()) {

                // Bundle to pass the data
                val bundle = Bundle().apply {
                    putString("imageUriString", pages[0].imageUri.toString())
                    putInt("pageId", viewModel.getPageId()!!)
                }

                // Navigate to the ResultFragment with the Bundle
                findNavController().navigate(
                    R.id.action_global_similarityScoreFragment, bundle
                )

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                requireContext(), getString(R.string.error_scanner_cancelled), Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.error_default_message), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun submitImage(imageUri: Uri?) {
        // Bundle to pass the data
        Log.d(TAG, "imageuri: $imageUri")
        val bundle = Bundle().apply {
            putString(
                "imageUriString", imageUri.toString()
            )
            putInt("pageId", viewModel.getPageId()!!)
        }

        // Navigate to the ResultFragment with the Bundle
        findNavController().navigate(
            R.id.action_global_similarityScoreFragment, bundle
        )
    }
}