package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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

    operator fun invoke(bitmap: Bitmap, onBarcodeSuccess: (String?) -> Unit, onBarcodeError: (Exception) -> Unit) {
        Log.d(TAG, "QR code scanning...")
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE)
            .build()

        val image = InputImage.fromBitmap(bitmap, 0)

//        val image = InputImage.fromFilePath(context, uri)

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                for (barcode in barcodes) {
                    Log.d(TAG, "Barcode value: ${barcode.rawValue}")
                    onBarcodeSuccess(barcode.rawValue)
//                    val bounds = barcode.boundingBox
//                    val corners = barcode.cornerPoints

//                    val rawValue = barcode.rawValue

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
                onBarcodeError(it)
            }
    }
}