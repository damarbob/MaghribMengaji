package com.simsinfotekno.maghribmengaji

//import android.R

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class DocScannerActivity : AppCompatActivity(), IOCRCallBack {

    private lateinit var scannerResultImageView: ImageView
    private lateinit var documentScannerButton: Button
    private lateinit var qrScannerButton: Button
    private lateinit var quranApiTextView: TextView

    private val mAPiKey = "K88528569888957"

    private var isOverlayRequired : Boolean = false
    private lateinit var mImageBase64: String
    private lateinit var mLanguage: String
    private lateinit var ocrResultTextView: TextView
    private lateinit var mIOCRCallBack: IOCRCallBack
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        scannerResultImageView = findViewById<ImageView>(R.id.imageViewScannerResult)
        documentScannerButton = findViewById<Button>(R.id.buttonDocumentScanner)
        qrScannerButton = findViewById<Button>(R.id.buttonQrScanner)
        quranApiTextView = findViewById<TextView>(R.id.textViewQuranApi)

        val scanner = GmsDocumentScanning.getClient(option.build())
        val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleActivityResult(result)
        }

        documentScannerButton.setOnClickListener {
            Glide.with(this).clear(scannerResultImageView)

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

        qrScannerButton.setOnClickListener {
            startActivity(Intent(this,QRScannerActivity::class.java))
        }
    }

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
                            mImageBase64 = bitmapToBase64(gambar)

                            // shadow removal
                            documentFilter.getLightenFilter(gambar) {
                                // Do your tasks here with the returned bitmap
                                scannerResultImageView.setImageBitmap(it)
                            }

                            FetchQuranPageTask().execute()
                            initOCR()

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
                    .into(scannerResultImageView)

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

    inner class FetchQuranPageTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
//    private fun FetchQuranPageTask() {
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

//    if (!result.isNullOrEmpty()) {
//                quranApiTextView.text = result
//            } else {
//                quranApiTextView.text = "Failed to fetch data"
//            }
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
    }

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

    private fun initOCR() {
        ocrResultTextView = findViewById<TextView>(R.id.textViewOCRResult)
            val oCRAsyncTask = OCRAsyncTask(
                this@DocScannerActivity,
                mAPiKey,
                isOverlayRequired,
                mImageBase64,
                mLanguage,
                mIOCRCallBack
            )
            oCRAsyncTask.execute()
        }


    override fun getOCRCallBackResult(response: String?) {
        ocrResultTextView.visibility = View.VISIBLE
        ocrResultTextView.text = response?.let { extractTextFromJsonOCRApi(it) }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        println(Base64.encodeToString(byteArray, Base64.DEFAULT))
        return "data:image/jpeg;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"
    }
}