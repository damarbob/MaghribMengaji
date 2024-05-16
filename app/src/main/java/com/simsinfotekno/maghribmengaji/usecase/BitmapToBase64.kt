package com.simsinfotekno.maghribmengaji.usecase

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class BitmapToBase64 {

    /**
     * Convert bitmap to Base64
     * TODO: Move to use case
     */
    operator fun invoke(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return "data:image/jpeg;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"
    }
}