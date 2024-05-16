package com.simsinfotekno.maghribmengaji.ui.page

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageBinding
import com.simsinfotekno.maghribmengaji.usecase.BitmapToBase64
import com.simsinfotekno.maghribmengaji.usecase.JaccardSimilarityIndex
import com.simsinfotekno.maghribmengaji.usecase.OCRAsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class PageFragment : Fragment(), IOCRCallBack {

    companion object {
        fun newInstance() = PageFragment()
    }

    private val viewModel: PageViewModel by viewModels()

    private var _binding: FragmentPageBinding? = null
    private val binding get() = _binding!!

    // OCR
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    private lateinit var quranApiResult: String
    private lateinit var ocrResult: String

    // Use case
    private val oCRAsyncTask = OCRAsyncTask()
    private val jaccardSimilarityIndex = JaccardSimilarityIndex()
    private val bitmapToBase64 = BitmapToBase64()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBinding.inflate(inflater, container, false)

        val pageId = arguments?.getInt("pageId")
        val thisQuranPage = MainActivity.quranPageRepository.getRecordById(pageId) // Get QuranPage instance
        val thisQuranVolume = MainActivity.quranVolumeRepository.getRecordByPageId(thisQuranPage!!.id) // Get QuranVolume instance

        // View
        binding.pageTextViewVolume.text = getString(R.string.quran_volume, thisQuranVolume?.id.toString())
        binding.pageTextViewPage.text = getString(R.string.quran_page, pageId.toString())
        binding.pageImageViewPage.setImageBitmap(thisQuranPage.picture)

        // Option for document scanning
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)

        // Get client and launcher of scanner
        val scanner = GmsDocumentScanning.getClient(option.build())
        val scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                handleActivityResult(result)
            }

        binding.pageButtonForward.setOnClickListener {

        }

        binding.pageButtonPrevious.setOnClickListener {

        }

        // Start scan
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

        return binding.root
    }

    // Handling activity result
    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            val pages = result.pages
            if (!pages.isNullOrEmpty()) {

                val documentFilter = DocumentFilter()

                // To convert URI to drawable->bitmap and to apply filter
                Glide
                    .with(this)
                    .load(pages[0].imageUri)
                    .listener(object : RequestListener<Drawable> {

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            val image = resource.toBitmap()

                            // Apply filter
                            documentFilter.getGreyScaleFilter(image) {

                                fetchQuranPageTask(1) // TODO: Change "1" to variable

                                // Do your tasks here with the returned bitmap
                                val mImageBase64 = bitmapToBase64(it)

                                // Start
                                oCRAsyncTask(
                                    requireActivity(),
                                    mImageBase64,
                                    "ara",
                                    this@PageFragment,
                                    binding.pageProgressBar,
                                    lifecycleScope
                                )
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
                    .into(binding.pageImageViewScannedResult) // Unused but don't delete

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
        binding.pageTextViewScore.text =
            jaccardSimilarityIndex(quranApiResult, ocrResult).toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}