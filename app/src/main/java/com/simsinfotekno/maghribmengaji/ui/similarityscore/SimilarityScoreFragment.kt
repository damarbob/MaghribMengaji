package com.simsinfotekno.maghribmengaji.ui.similarityscore

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simsinfotekno.maghribmengaji.IOCRCallBack
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class SimilarityScoreFragment : Fragment(), IOCRCallBack {

    companion object {
        fun newInstance() = SimilarityScoreFragment()
    }

    private val viewModel: SimilarityScoreViewModel by viewModels()

    private var _binding: FragmentSimilarityScoreBinding? = null
    private val binding get() = _binding!!

    // OCR
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    private lateinit var quranApiResult: String
    private lateinit var ocrResult: String

    // Use case
    private val oCRAsyncTask = OCRAsyncTask()
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSimilarityScoreBinding.inflate(inflater,container,false)

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

        binding.similarityScoreButtonUpload.setOnClickListener {

        }

        binding.similarityScoreButtonRetry.setOnClickListener {

        }

        binding.similarityScoreButtonClose.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    /**
     * Fetch Quran API
     */
    private fun fetchQuranPageTask(page: Int) {
        myExecutor.execute {
            var result = ""
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("https://api.alquran.cloud/v1/page/$page/quran-uthmani")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
                bufferedReader.close()
                result = stringBuilder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }

            myHandler.post {
                if (result.isNotEmpty()) {
                    quranApiResult = extractTextFromQuranApiJson(result)
//                    Toast.makeText(requireContext(),quranApiResult,Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.failed_to_fetch_data),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Extract text from Quran API JSON
     * TODO: Move to use case
     */
    private fun extractTextFromQuranApiJson(jsonString: String): String {
        val jsonObject = JSONObject(jsonString)
        val ayahsArray = jsonObject.getJSONObject("data").getJSONArray("ayahs")
        val stringBuilder = StringBuilder()
        for (i in 0 until ayahsArray.length()) {
            val ayahObject = ayahsArray.getJSONObject(i)
            val ayahText = ayahObject.getString("text")
            stringBuilder.append(ayahText).append("\n")
        }
        return stringBuilder.toString()
    }

    /**
     * Extract text from OCR API JSON
     * TODO: Move to use case
     */
    private fun extractTextFromOCRApiJson(jsonString: String): String? {
        try {
            val jsonObject = JSONObject(jsonString)
            val parsedResults = jsonObject.getJSONArray("ParsedResults")
            if (parsedResults.length() > 0) {
                val firstResult = parsedResults.getJSONObject(0)
                return firstResult.optString("ParsedText")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {
        ocrResult = response?.let {
            extractTextFromOCRApiJson(it)
        }.toString()
        val similarityIndex = jaccardSimilarityIndex(quranApiResult,ocrResult)
        binding.similarityScoreTextViewScore.text =
            similarityIndex.toInt().toString()
        binding.similarityScoreCircularProgressScore.progress = similarityIndex.toInt()
        if (similarityIndex > 70) {
            binding.similarityScoreTextViewDetail.text = getString(R.string.your_score_is_ssr_press_upload_to_send_your_score)
        } else binding.similarityScoreTextViewDetail.text = getString(R.string.your_score_is_low)

    }
}