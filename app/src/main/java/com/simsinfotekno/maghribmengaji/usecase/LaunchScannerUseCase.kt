package com.simsinfotekno.maghribmengaji.usecase

import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

class LaunchScannerUseCase {

    companion object {
        private val TAG = LaunchScannerUseCase::class.java.simpleName
    }

    operator fun invoke(fragment: Fragment, scannerLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        // Option for document scanning
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setPageLimit(1)
            .build()

        // Get the document scanner client
        val scanner = GmsDocumentScanning.getClient(option)

        Log.d(TAG, "invoke()")

        // Start scanner intent
        scanner.getStartScanIntent(fragment.requireActivity())
            .addOnSuccessListener { intentSender ->
                Log.d(TAG, "launching scanner")
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                Log.d(TAG, "scanner launched")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "failed to launch scanner")
                Toast.makeText(fragment.context, exception.message, Toast.LENGTH_LONG).show()
            }
    }
}
