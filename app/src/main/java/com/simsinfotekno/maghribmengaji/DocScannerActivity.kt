package com.simsinfotekno.maghribmengaji

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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


class DocScannerActivity : AppCompatActivity() {

    private lateinit var scannerResultImageView: ImageView
    private lateinit var documentScannerButton: Button
    private lateinit var qrScannerButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            // shadow removal
                            documentFilter.getLightenFilter(gambar) {
                                // Do your tasks here with the returned bitmap
                                scannerResultImageView.setImageBitmap(it)
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
}