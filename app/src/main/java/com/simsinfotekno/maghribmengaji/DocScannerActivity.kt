package com.simsinfotekno.maghribmengaji

//import android.R

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.databinding.ActivityDocscannerBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.round


class DocScannerActivity : AppCompatActivity(), IOCRCallBack {

    private lateinit var binding: ActivityDocscannerBinding

//    private lateinit var scannerResultImageView: ImageView
//    private lateinit var documentScannerButton: Button
//    private lateinit var qrScannerButton: Button
//    private lateinit var quranApiTextView: TextView
//    private lateinit var resultTextView: TextView

    private val mAPiKey = "K88528569888957"

    private var isOverlayRequired : Boolean = false
//    private lateinit var mImageBase64: String
    private var mImageBase64: String = ""
    private lateinit var mLanguage: String
//    private lateinit var ocrResultTextView: TextView
    private lateinit var mIOCRCallBack: IOCRCallBack
//    private lateinit var progressBar: ProgressBar

    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDocscannerBinding.inflate(layoutInflater)

        mIOCRCallBack = this
        mLanguage = "ara" //Language
        isOverlayRequired = false

//      option for document scanning
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_BASE_WITH_FILTER)
            .setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setPageLimit(1)

        enableEdgeToEdge()
        setContentView(R.layout.activity_docscanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        scannerResultImageView = findViewById(R.id.imageViewScannerResult)
//        documentScannerButton = findViewById(R.id.buttonDocumentScanner)
//        qrScannerButton = findViewById(R.id.buttonQrScanner)
//        quranApiTextView = findViewById(R.id.textViewQuranApi)
//        ocrResultTextView = findViewById(R.id.textViewOCRResult)
//        resultTextView = findViewById(R.id.textViewResult)
//        progressBar = findViewById(R.id.progressBar)

//      get client and launcher of scanner
        val scanner = GmsDocumentScanning.getClient(option.build())
        val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleActivityResult(result)
        }

        binding.buttonDocumentScanner.setOnClickListener {
            Glide.with(this).clear(binding.imageViewScannerResult)

            scanner.getStartScanIntent(this)
                .addOnSuccessListener {
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        binding.buttonQrScanner.setOnClickListener {
            startActivity(Intent(this,QRScannerActivity::class.java))
        }

    }

    /**
    * Handle the result from scanner
    */
    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            val pages = result.pages
            if (!pages.isNullOrEmpty()) {

                val documentFilter = DocumentFilter()

//                Glide
//                    .with(this)
//                    .load(pages[0].imageUri)
//                    .into(scannerResultImageView)
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
                            val gambar = resource.toBitmap()
//                            mImageBase64 = bitmapToBase64(gambar)

                            // shadow removal
                            documentFilter.getGreyScaleFilter(gambar) {
                                // Do your tasks here with the returned bitmap
                                binding.imageViewScannerResult.setImageBitmap(it)
                                mImageBase64 = bitmapToBase64(it)

    //                          FetchQuranPageTask().execute()
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
                    .into(binding.imageViewScannerResult)

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_scanner_cancelled),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.error_default_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
                binding.textViewQuranApi.visibility = View.VISIBLE
                if (result.isNotEmpty()) {
                    binding.textViewQuranApi.text = extractTextFromJsonQuranApi(result)
                } else {
                    binding.textViewQuranApi.text = getString(R.string.failed_to_fetch_data)
                }
            }
        }
    }

    /*inner class FetchQuranPageTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
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

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            quranApiTextView.visibility = View.VISIBLE
            if (!result.isNullOrEmpty()) {
                quranApiTextView.text = extractTextFromJsonQuranApi(result)
            } else {
                quranApiTextView.text = "Failed to fetch data"
            }
        }
    }*/

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
            this@DocScannerActivity,
            mAPiKey,
            isOverlayRequired,
            mImageBase64,
            mLanguage,
            mIOCRCallBack
        )
//            oCRAsyncTask.execute()
        lifecycleScope.launch {
            oCRAsyncTask.executeAsyncTask(binding.progressBar)
        }
    }


    /**
     * Get OCR Callback result
     */
    override fun getOCRCallBackResult(response: String?) {
        binding.textViewOCRResult.visibility = View.VISIBLE
        binding.textViewOCRResult.text = response?.let { extractTextFromJsonOCRApi(it) }

        val quranApiText = binding.textViewQuranApi.text.toString()
        val ocrText = binding.textViewOCRResult.text.toString()
        binding.textViewResult.visibility = View.VISIBLE
        binding.textViewResult.text = calculateSimilarityIndex(quranApiText,ocrText).toString()
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
}