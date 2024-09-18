package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher

class LaunchCameraUseCase {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    companion object {
        private val TAG = LaunchCameraUseCase::class.java.simpleName
    }

    operator fun invoke(context: Context, cameraLauncher: ActivityResultLauncher<Intent>) {
        this.cameraLauncher = cameraLauncher
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(context.packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            }
    }
}