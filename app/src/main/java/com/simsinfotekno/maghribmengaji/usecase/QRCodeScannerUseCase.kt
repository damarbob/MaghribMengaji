package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simsinfotekno.maghribmengaji.ui.similarityscore.SimilarityScoreViewModel
import com.simsinfotekno.maghribmengaji.ui.similarityscore.SimilarityScoreViewModel.Companion
import java.io.IOException

class QRCodeScannerUseCase {
    companion object {
        val TAG = QRCodeScannerUseCase::class.simpleName
        const val QR_CODE_NOT_FOUND = 1010
    }

    operator fun invoke(
        bitmap: Bitmap,
        onBarcodeSuccess: (String?) -> Unit,
        onBarcodeError: (Exception) -> Unit
    ) {
        Log.d(TAG, "QR code scanning...")

        // Define timeout in milliseconds (30 seconds)
        val timeoutMillis: Long = 30_000

        // Create a handler to manage the timeout
        val handler = Handler(Looper.getMainLooper())
        var isCompleted = false

        // Define a timeout Runnable that will trigger if the scan is not complete in time
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                Log.d(TAG, "QR code scanning timed out.")
                onBarcodeError(Exception("QR code scanning timed out."))
            }
        }

        // Start the timeout countdown
        handler.postDelayed(timeoutRunnable, timeoutMillis)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val image = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (!isCompleted) {
                    isCompleted = true
                    handler.removeCallbacks(timeoutRunnable) // Cancel timeout if successful

                    if (barcodes.isNotEmpty()) {
                        for (barcode in barcodes) {
                            Log.d(TAG, "Barcode value: ${barcode.rawValue}")
                            onBarcodeSuccess(barcode.rawValue)
                            break
                        }
                    } else {
                        onBarcodeSuccess(null)
                    }
                }
            }
            .addOnFailureListener {
                if (!isCompleted) {
                    isCompleted = true
                    handler.removeCallbacks(timeoutRunnable) // Cancel timeout if an error occurs
                    onBarcodeError(it)
                }
            }
    }

}