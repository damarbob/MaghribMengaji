package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

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