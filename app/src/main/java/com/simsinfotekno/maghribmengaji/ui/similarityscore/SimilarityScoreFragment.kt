package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromOCRApiJSON
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromQuranAPIJSON
import com.simsinfotekno.maghribmengaji.usecase.FetchQuranPageUseCase
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.LaunchScannerUseCase
import com.simsinfotekno.maghribmengaji.usecase.LoadBitmapFromUri
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: set timeout, fix when no connection on upload page (maybe page document created but image not uploaded? however it fixed on relaunch app)
class SimilarityScoreFragment : Fragment(), FetchQuranPageUseCase.ResultHandler,
    OCRAsyncTask.IOCRCallBack, ActivityResultCallback<ActivityResult> {

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
    private var pageId: Int? = null
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: Uri
    private lateinit var container: ViewGroup
    private var similarityIndex: Int = 0

    /* Use cases */
    private val loadBitmapFromUri = LoadBitmapFromUri()
    private val oCRAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase(this)
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()
    private val bitmapToBase64 = BitmapToBase64()

    // TODO: KKM decided by ustadh
    private val kkm = 60

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var materialAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the scanner launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
            this
        )

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        materialAlertDialog = MaterialAlertDialogBuilder(requireContext()).create()

        // Set up the back button callback
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.your_result_will_be_gone))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        findNavController().popBackStack()
                    }
                    .show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSimilarityScoreBinding.inflate(inflater, container, false)

        this.container = container!!

        /* Observers */

        // Listening to remote db result whether success or failed
        viewModel.remoteDbResult.observe(viewLifecycleOwner) { uploadResult ->
            uploadResult?.onSuccess {

                // Navigate to the next screen or update UI
                Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_successful),
                    Toast.LENGTH_SHORT
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

        /* Variables and arguments */

        // Get image uri from view model if any, if not, get from arguments
        // Check if similarityIndex is already defined in ViewModel
        viewModel.similarityIndex.observe(viewLifecycleOwner) { index ->
            index?.let {
                // Maximize view and show score if similarityIndex is already defined
                maximizeView(maximized = true, scorePassed = it > kkm, onCreateView = false)
                binding.similarityScoreTextViewScore.text = it.toString()
                binding.similarityScoreCircularProgressScore.progress = it
                binding.similarityScoreCircularProgress.visibility = View.GONE
                similarityIndex = it
            }
        }
        viewModel.progressVisibility.observe(viewLifecycleOwner) { isVisible ->
            binding.similarityScoreCircularProgress.visibility =
                if (isVisible) View.VISIBLE else View.GONE
            binding.similarityScoreCircularProgressScore.visibility =
                if (isVisible) View.GONE else View.VISIBLE
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


        // Define image Uri
        val imageUriString: String
        if (viewModel.imageUriString != null) {
            imageUriString = viewModel.imageUriString!!
        } else {
            imageUriString = arguments?.getString("imageUriString")!!
            viewModel.imageUriString = imageUriString
        }
        imageUri = Uri.parse(imageUriString)

        // Get page id from view model if any, if not, get from arguments
        if (viewModel.pageId != null) {
            pageId = viewModel.pageId
        } else {
            pageId = arguments?.getInt("pageId") ?: -1
            viewModel.pageId = arguments?.getInt("pageId")
        }

        // Get bitmap from view model if any, if not, get from arguments
        if (viewModel.bitmap != null) {
            bitmap =
                viewModel.bitmap // Get bitmap from view model (if it exists, it the fragment might had restarted)
        } else {

            // Load new bitmap from uri (local)
            bitmap = loadBitmapFromUri(requireContext(), imageUri)

            // Apply image filter
            applyImageFilter2 {
                viewModel.bitmap = it // Assign bitmap to view model to survive fragment reload
                bitmap = it // Reassign bitmap with the new ones (with filter)

                // Start OCR API
                oCRAsyncTask(
                    bitmapToBase64(it!!),
                    "ara",
                    this@SimilarityScoreFragment,
                    binding.similarityScoreCircularProgress,
                    lifecycleScope
                )
            }
        }

        /* View */
        maximizeView(
            maximized = false,
            scorePassed = false,
            onCreateView = true
        ) // Show only progress indicator and close button

        if (pageId != -1) {
            fetchQuranPageTask(pageId!!)
        }

        /* Listeners */
        binding.similarityScoreButtonUpload.setOnClickListener {
            materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.your_similarity_score, similarityIndex.toString()))
                .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    viewModel.uploadPageStudent()
                    binding.similarityScoreButtonUpload.isEnabled = false
                    binding.similarityScoreButtonUploadLow.isEnabled = false
                    binding.similarityScoreButtonRetry.isEnabled = false
                    binding.similarityScoreButtonRetryLow.isEnabled = false
                }
                .show()
        }

        binding.similarityScoreButtonUploadLow.setOnClickListener {
            materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.your_similarity_score, similarityIndex.toString()))
                .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    viewModel.uploadPageStudent()
                    binding.similarityScoreButtonUpload.isEnabled = false
                    binding.similarityScoreButtonUploadLow.isEnabled = false
                    binding.similarityScoreButtonRetry.isEnabled = false
                    binding.similarityScoreButtonRetryLow.isEnabled = false
                }
                .show()
        }

        binding.similarityScoreButtonRetry.setOnClickListener {
            materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.are_you_sure))
                .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    LaunchScannerUseCase().invoke(this, scannerLauncher)
                }
                .show()
        }

        binding.similarityScoreButtonRetryLow.setOnClickListener {
            materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.are_you_sure))
                .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                    LaunchScannerUseCase().invoke(this, scannerLauncher)
                }
                .show()
        }

        binding.similarityScoreButtonClose.setOnClickListener {
//            if (binding.similarityScoreButtonUpload.visibility == View.GONE || binding.similarityScoreButtonUploadLow.visibility == View.GONE || binding.similarityScoreCircularProgress.visibility == View.VISIBLE) {
            if (binding.similarityScoreTextViewScore.visibility == View.GONE || binding.similarityScoreCircularProgress.visibility == View.VISIBLE) {
                backPressedCallback.handleOnBackPressed()
            } else findNavController().popBackStack()
        }

        return binding.root
    }

    private fun maximizeView(maximized: Boolean, scorePassed: Boolean, onCreateView: Boolean) {
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
            if (maximized) View.VISIBLE else View.GONE
    }

    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {
        lifecycleScope.launch {
            ocrResult = withContext(Dispatchers.IO) {
                response?.let { extractTextFromOCRApiJson(it) }.toString()
            }

            // Wait until quranApiResult is ready
            quranApiResult = quranApiResultDeferred.await()

            // Ensure quranApiResult is ready before calculating similarity index
            calculateSimilarityIndex()

        }
    }

    override fun onOCRFailure(exception: Exception) {
        // Check if binding is null before using it
        if (_binding != null) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_LONG).show()
            }
            maximizeView(true, scorePassed = false, onCreateView = false)
            Log.d(TAG, exception.toString())
        } else {
            Log.e(TAG, "Binding is null in onOCRFailure: $exception")
        }
    }

    /**
     * Get Quran API result
     */
    override fun onSuccess(result: String) {
        // Handle success, e.g., update UI
        lifecycleScope.launch {
            val extractedQuranResult = withContext(Dispatchers.IO) {
                extractTextFromQuranApiJson(result)
            }
//            quranApiResult = extractedQuranResult
            quranApiResultDeferred.complete(extractedQuranResult)

            // If ocrResult is already ready, calculate similarity index
//            if (::ocrResult.isInitialized) {
            if (::ocrResult != null) {
                calculateSimilarityIndex()
            }
        }
    }

    // Fetch Quran API Result Handler on failure
    override fun onFailure(message: String) {
        // Check if binding is null before using it
        if (_binding != null) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
            maximizeView(true, scorePassed = false, onCreateView = false)
            Log.d(TAG, message)
        } else {
            Log.e(TAG, "Binding is null in onQuranAPIResultFailure: $message")
        }
    }

    /*
    * Calculate similarity index and update UI
    */
    private suspend fun calculateSimilarityIndex() {
        withContext(Dispatchers.Main) {
            val similarityIndex = quranApiResult?.let { ocrResult?.let { it1 ->
                jaccardSimilarityIndex(it,
                    it1
                )
            } }

            viewModel.oCRScore = similarityIndex // Set OCR score in view model

            // Save similarityIndex to ViewModel
            viewModel.similarityIndex.value = similarityIndex?.toInt()

            // Maximize view and show score
            if (similarityIndex != null) {
                if (similarityIndex > kkm) {
                    binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                        getString(R.string.your_score_is_great_press_upload_to_send_your_score),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    maximizeView(maximized = true, scorePassed = true, onCreateView = false)
                } else {
                    binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                        getString(R.string.your_score_is_low),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    maximizeView(maximized = true, scorePassed = false, onCreateView = false)
                }
            }

            // Add animation
            if (similarityIndex != null) {
                ValueAnimator.ofInt(similarityIndex.toInt()).apply {
                    duration = 1000
                    addUpdateListener {
                        val animationValue = it.animatedValue as Int
                        binding.similarityScoreTextViewScore.text = animationValue.toString()
                        binding.similarityScoreCircularProgressScore.progress = animationValue
                    }
                }.start()
            }
        }
    }

    private fun applyImageFilter2(documentFilterCallback: DocumentFilter.CallBack<Bitmap>) {
        val documentFilter = DocumentFilter()
        documentFilter.getGreyScaleFilter(bitmap, documentFilterCallback)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        materialAlertDialog?.dismiss()
        _binding = null
        quranApiResultDeferred.cancel()
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
                    putInt("pageId", pageId!!)
                }

                // Navigate to the ResultFragment with the Bundle
                findNavController().navigate(
                    R.id.action_global_similarityScoreFragment,
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
}