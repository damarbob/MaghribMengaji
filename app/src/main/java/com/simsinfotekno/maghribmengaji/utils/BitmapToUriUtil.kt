package com.simsinfotekno.maghribmengaji.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
object BitmapToUriUtil {

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): Uri? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(null), filename)

        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(format, quality, fos)
            fos.flush()
            fos.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}