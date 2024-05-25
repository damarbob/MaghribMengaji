package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.animation.ValueAnimator
import android.content.res.Resources.Theme
import android.content.res.Resources.getAttributeSetSourceResId
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromOCRApiJSON
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromQuranAPIJSON
import com.simsinfotekno.maghribmengaji.usecase.FetchQuranPageUseCase
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.LoadBitmapFromUri
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimilarityScoreFragment : Fragment(), FetchQuranPageUseCase.ResultHandler,
    OCRAsyncTask.IOCRCallBack {

    companion object {
        private val TAG = SimilarityScoreFragment::class.java.simpleName
        fun newInstance() = SimilarityScoreFragment()
    }

    private val viewModel: SimilarityScoreViewModel by viewModels()

    private var _binding: FragmentSimilarityScoreBinding? = null
    private val binding get() = _binding!!

    // Repository
    private val quranPageStudentRepository = MainActivity.quranPageStudentRepository

    // OCR
    private lateinit var quranApiResult: String
    private lateinit var ocrResult: String

    // Use case
    private val loadBitmapFromUri = LoadBitmapFromUri()
    private val oCRAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase(this)
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()
    private val bitmapToBase64 = BitmapToBase64()

    // Global variables
    private var pageId: Int? = null
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: Uri

    // Experimental
    private val student = MainActivity.testStudent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSimilarityScoreBinding.inflate(inflater, container, false)

        val uriString = arguments?.getString("image_uri")
        imageUri = Uri.parse(uriString)

        // Define page ID
        if (viewModel.pageId != null) {
            pageId = viewModel.pageId
        } else {
            pageId = arguments?.getInt("pageId") ?: -1
            viewModel.pageId = arguments?.getInt("pageId")
        }

        // Define image
        if (viewModel.bitmap != null) {
            bitmap = viewModel.bitmap!!
        } else {
            applyImageFilter2{
                viewModel.bitmap = it
                bitmap = it

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
        maximizeView(false, true) // Show only progress indicator and close button

        if (pageId != -1) {
            fetchQuranPageTask(pageId!!)
        }

        /* Listeners */
        binding.similarityScoreButtonUpload.setOnClickListener {
            uploadPageStudent()
        }

        binding.similarityScoreButtonUploadLow.setOnClickListener {
            uploadPageStudent()
        }

        binding.similarityScoreButtonRetry.setOnClickListener {

        }

        binding.similarityScoreButtonRetryLow.setOnClickListener {

        }

        binding.similarityScoreButtonClose.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    // TODO: move to use case
    private fun uploadPageStudent() {
        val pageStudent = quranPageStudentRepository.getRecordByPageId(pageId)

        if (pageStudent != null)
            pageStudent.picture = bitmap
        else
            quranPageStudentRepository.addRecord(
                QuranPageStudent(
                    pageId!!,
                    student.id,
                    null,
                    picture = bitmap
                )
            )

        Log.d(TAG, "${quranPageStudentRepository.getRecordByPageId(pageId)}")
    }

    private fun maximizeView(maximized: Boolean, scorePassed: Boolean) {
        // Whether to show all or only progress indicator and close button
        binding.similarityScoreTextView.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewScore.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreButtonUpload.visibility = if (maximized && scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonRetry.visibility = if (maximized && scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewDetail.visibility = if (maximized && scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonUploadLow.visibility = if (maximized && !scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreButtonRetryLow.visibility = if (maximized && !scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewDetailLow.visibility = if (maximized && !scorePassed) View.VISIBLE else View.GONE
        binding.similarityScoreCircularProgressScore.visibility = if (maximized) View.VISIBLE else View.GONE
    }

    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {
        lifecycleScope.launch {
            val extractedOCRResult = withContext(Dispatchers.IO) {
                response?.let { extractTextFromOCRApiJson(it) }.toString()
            }
            ocrResult = extractedOCRResult

            // Ensure quranApiResult is ready before calculating similarity index
            calculateSimilarityIndex()
        }
    }

    override fun onOCRFailure(exception: Exception) {
        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
        Log.d(TAG, exception.message!!)
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
            quranApiResult = extractedQuranResult

            // If ocrResult is already ready, calculate similarity index
            if (::ocrResult.isInitialized) {
                calculateSimilarityIndex()
            }
        }
    }

    // Fetch Quran API Result Handler on failure
    override fun onFailure(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        Log.d(TAG, message)
    }

    /*
    * Calculate similarity index and update UI
    */
    private suspend fun calculateSimilarityIndex() {
        withContext(Dispatchers.Main) {
            val similarityIndex = jaccardSimilarityIndex(quranApiResult, ocrResult)

            // Maximize view and show score
            if (similarityIndex > 70) {
                binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                    getString(R.string.your_score_is_ssr_press_upload_to_send_your_score),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                maximizeView(true, true)
            } else {
                binding.similarityScoreTextViewDetail.text = HtmlCompat.fromHtml(
                    getString(R.string.your_score_is_low),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                maximizeView(true, false)
            }

            // Add animation
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

    private fun applyImageFilter2(documentFilterCallback: DocumentFilter.CallBack<Bitmap>) {
        bitmap = loadBitmapFromUri(requireContext(), imageUri)

        val documentFilter = DocumentFilter()
        documentFilter.getGreyScaleFilter(bitmap, documentFilterCallback)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}