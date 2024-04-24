package com.simsinfotekno.maghribmengaji.ui.page

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.IOCRCallBack
import com.simsinfotekno.maghribmengaji.OCRAsyncTask2
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.round

class PageFragment : Fragment(), IOCRCallBack {

    companion object {
        fun newInstance() = PageFragment()
    }

    private val viewModel: PageViewModel by viewModels()

    private var _binding: FragmentPageBinding? = null
    private val binding get() = _binding!!

    // OCR
    private val mAPiKey = "K88528569888957"
    private var isOverlayRequired : Boolean = false
    private lateinit var mImageBase64: String
    private lateinit var mLanguage: String
    private lateinit var mIOCRCallBack: IOCRCallBack

    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    private lateinit var quranApiResult: String
    private lateinit var ocrResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        OCR
        mIOCRCallBack = this
        mLanguage = "ara" //Language
        isOverlayRequired = false

//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED){
//
//            }
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBinding.inflate(inflater,container,false)

        //        option for document scanning
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)

//        get client and launcher of scanner
        val scanner = GmsDocumentScanning.getClient(option.build())
        val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleActivityResult(result)
        }

        binding.pageButtonForward.setOnClickListener {

        }

        binding.pageButtonPrevious.setOnClickListener {

        }

//        start scan
        binding.pageButtonSubmit.setOnClickListener {
            scanner.getStartScanIntent(this.requireActivity())
                .addOnSuccessListener {
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            val pages = result.pages
            if (!pages.isNullOrEmpty()) {

                val documentFilter = DocumentFilter()

                Glide
                    .with(this)
                    .load(pages[0].imageUri)
                    .listener(object : RequestListener<Drawable> {

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            val image = resource.toBitmap()

                            // apply filter
                            documentFilter.getGreyScaleFilter(image) {
                                // Do your tasks here with the returned bitmap
                                mImageBase64 = bitmapToBase64(it)

                                fetchQuranPageTask()
                                initOCR()
                            }

                            return true
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.pageImageViewScannedResult)

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

    /**
    * Fetch Quran API
    */
    private fun fetchQuranPageTask(){
        myExecutor.execute {
            var result = ""
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("https://api.alquran.cloud/v1/page/1/quran-uthmani")
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
//                binding.textViewQuranApi.visibility = View.VISIBLE
//                if (result.isNotEmpty()) {
//                    binding.textViewQuranApi.text = extractTextFromJsonQuranApi(result)
//                } else {
//                    binding.textViewQuranApi.text = getString(R.string.failed_to_fetch_data)
//                }
                if (result.isNotEmpty()) {
                    quranApiResult = extractTextFromJsonQuranApi(result)
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
     */
    private fun extractTextFromJsonQuranApi(jsonString: String): String {
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
     */
    private fun extractTextFromJsonOCRApi(jsonString: String): String? {
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
     * Call OCR API
     */
    private fun initOCR() {

        val oCRAsyncTask = OCRAsyncTask2(
//            val oCRAsyncTask = OCRAsyncTask(
            this.requireActivity(),
            mAPiKey,
            isOverlayRequired,
            mImageBase64,
            mLanguage,
            mIOCRCallBack
        )
//            oCRAsyncTask.execute()
        lifecycleScope.launch {
            oCRAsyncTask.executeAsyncTask(binding.pageProgressBar)
        }
    }


    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {
//        binding.textViewOCRResult.visibility = View.VISIBLE
//        binding.textViewOCRResult.text = response?.let { extractTextFromJsonOCRApi(it) }
//
//        val quranApiText = binding.textViewQuranApi.text.toString()
//        val ocrText = binding.textViewOCRResult.text.toString()
//        binding.textViewResult.visibility = View.VISIBLE
//        binding.textViewResult.text = calculateSimilarityIndex(quranApiText,ocrText).toString()
        ocrResult = response?.let {
            extractTextFromJsonOCRApi(it) }.toString()
        binding.pageTextViewScore.text = calculateSimilarityIndex(quranApiResult,ocrResult).toString()
    }

    /**
     * Convert bitmap to Base64
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
//        println(Base64.encodeToString(byteArray, Base64.DEFAULT))
        return "data:image/jpeg;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"
    }

    /**
     * Remove harakats or diacritics from string
     */
    private fun removeDiacritics(input: String): String {
        val diacritics = listOf('\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652', '\u0670')
        val builder = StringBuilder()
        input.forEach { char ->
            if (!diacritics.contains(char)) {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    fun calculateSimilarityIndexA(str1: String, str2: String): Double {
        val cleanStr1 = removeDiacritics(str1)
        val cleanStr2 = removeDiacritics(str2)

        val maxLength = maxOf(cleanStr1.length, cleanStr2.length)
        var similarity = 0.0

        for (i in 0 until maxLength) {
            val char1 = if (i < cleanStr1.length) cleanStr1[i] else '\u0000'
            val char2 = if (i < cleanStr2.length) cleanStr2[i] else '\u0000'

            if (char1 == char2) {
                similarity++
            }
        }

        return similarity / maxLength
    }

    /**
     * Calculate similarity index of 2 strings
     * with Jaccard method
     */
    private fun calculateSimilarityIndex(str1: String, str2: String): Double {
        val cleanStr1 = removeDiacritics(str1).toSet()
        val cleanStr2 = removeDiacritics(str2).toSet()

        val intersectionSize = cleanStr1.intersect(cleanStr2).size.toDouble()
        val unionSize = cleanStr1.union(cleanStr2).size.toDouble()

        return round(intersectionSize / unionSize * 1000) / 10
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}