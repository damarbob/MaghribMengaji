package com.simsinfotekno.maghribmengaji.usecase

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class LaunchCameraUseCase {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private val TAG = LaunchCameraUseCase::class.java.simpleName
    }

    operator fun invoke(context: Context, cameraLauncher: ActivityResultLauncher<Intent>, requestCameraPermissionLauncher: ActivityResultLauncher<String>) {
        this.cameraLauncher = cameraLauncher
        this.requestCameraPermissionLauncher = requestCameraPermissionLauncher
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(context.packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}