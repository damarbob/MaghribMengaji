package com.simsinfotekno.maghribmengaji

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrscanner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun barcodeScanner(bitmap: Bitmap) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE)
            .build()

        val image = InputImage.fromBitmap(bitmap, 0)

        val scanner = BarcodeScanning.getClient(options)

        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue

//                    val valueType = barcode.valueType
                    // See API reference for complete list of supported types
//                    when (valueType) {
//                        Barcode.TYPE_WIFI -> {
//                            val ssid = barcode.wifi!!.ssid
//                            val password = barcode.wifi!!.password
//                            val type = barcode.wifi!!.encryptionType
//                        }
//                        Barcode.TYPE_URL -> {
//                            val title = barcode.url!!.title
//                            val url = barcode.url!!.url
//                        }
//                    }
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }
    }

    private fun gmsScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // Task completed successfully
                Toast.makeText(
                    applicationContext,
                    barcode.rawValue,
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCanceledListener {
                // Task canceled
                Toast.makeText(
                    applicationContext,
                    getString(R.string.error_scanner_cancelled),
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(
                    applicationContext,
                    "${getString(R.string.error_default_message)} ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}