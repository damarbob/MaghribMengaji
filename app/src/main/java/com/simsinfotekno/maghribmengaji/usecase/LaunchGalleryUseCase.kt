package com.simsinfotekno.maghribmengaji.usecase

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class LaunchGalleryUseCase {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestGalleryPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private val TAG = LaunchGalleryUseCase::class.java.simpleName
    }

    operator fun invoke(context: Context, galleryLauncher: ActivityResultLauncher<Intent>, requestGalleryPermissionLauncher: ActivityResultLauncher<String>) {
        this.galleryLauncher = galleryLauncher
        this.requestGalleryPermissionLauncher = requestGalleryPermissionLauncher
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(pickPhotoIntent)
        } else {
            requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}