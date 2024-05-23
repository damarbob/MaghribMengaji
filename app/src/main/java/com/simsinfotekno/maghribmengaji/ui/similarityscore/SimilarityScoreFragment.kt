package com.simsinfotekno.maghribmengaji.ui.similarityscore

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromOCRApiJSON
import com.simsinfotekno.maghribmengaji.usecase.ExtractTextFromQuranAPIJSON
import com.simsinfotekno.maghribmengaji.usecase.FetchQuranPageUseCase
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask

class SimilarityScoreFragment : Fragment(), OCRAsyncTask.IOCRCallBack,
    FetchQuranPageUseCase.ResultHandler {

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
    private val oCRAsyncTask = OCRAsyncTask()
    private val fetchQuranPageTask = FetchQuranPageUseCase(this)
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val extractTextFromQuranApiJson = ExtractTextFromQuranAPIJSON()
    private val extractTextFromOCRApiJson = ExtractTextFromOCRApiJSON()

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

        val pageId = arguments?.getInt("pageId")
        val mImageBase64 = arguments?.getString("image_base64")
        fetchQuranPageTask(pageId!!)

        // Start
        oCRAsyncTask(
            requireActivity(),
            mImageBase64!!,
            "ara",
            this@SimilarityScoreFragment,
            binding.similarityScoreCircularProgress,
            lifecycleScope
        )

        /* View */
        maximizeView(false) // Show only progress indicator and close button

        /* Listeners */
        binding.similarityScoreButtonUpload.setOnClickListener {
            val pageStudent = quranPageStudentRepository.getRecordByPageId(pageId)
            val decodedByte = Base64.decode(mImageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)

            if (pageStudent != null)
                pageStudent.picture = bitmap
            else
                quranPageStudentRepository.addRecord(
                    QuranPageStudent(
                        pageId,
                        student.id,
                        null,
                        picture = bitmap
                    )
                )

            Log.d(TAG, "${quranPageStudentRepository.getRecordByPageId(pageId)}")

        }

        binding.similarityScoreButtonRetry.setOnClickListener {

        }

        binding.similarityScoreButtonClose.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    private fun maximizeView(maximized: Boolean) {
        // Whether to show all or only progress indicator and close button
        binding.similarityScoreTextView.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewScore.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreButtonUpload.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreButtonRetry.visibility = if (maximized) View.VISIBLE else View.GONE
        binding.similarityScoreTextViewDetail.visibility = if (maximized) View.VISIBLE else View.GONE
    }

    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {

        ocrResult = response?.let {
            extractTextFromOCRApiJson(it)
        }.toString()

        val similarityIndex = jaccardSimilarityIndex(quranApiResult, ocrResult)

        // Maximize view and show score
        maximizeView(true)

        binding.similarityScoreTextViewScore.text =
            similarityIndex.toInt().toString()
        binding.similarityScoreCircularProgressScore.progress = similarityIndex.toInt()
        if (similarityIndex > 70) {
            binding.similarityScoreTextViewDetail.text =
                getString(R.string.your_score_is_ssr_press_upload_to_send_your_score)
        } else binding.similarityScoreTextViewDetail.text = getString(R.string.your_score_is_low)

    }

    override fun onSuccess(result: String) {
        quranApiResult = extractTextFromQuranApiJson(result)
        // Handle success, e.g., update UI
        // Toast.makeText(this, quranApiResult, Toast.LENGTH_LONG).show()
    }

    override fun onFailure(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}