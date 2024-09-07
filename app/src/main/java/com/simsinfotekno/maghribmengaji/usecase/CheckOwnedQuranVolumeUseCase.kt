package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.R

class CheckOwnedQuranVolumeUseCase {

    companion object {
        private val TAG = CheckOwnedQuranVolumeUseCase::class.simpleName
    }

    operator fun invoke(
        ownedVolumeId: List<Int>?,
        volumeId: Int? = null, // Volume Id for comparison
        pageId: Int? = null, // Page Id for comparison
        onOwnedVolume: () -> Unit = {},
        onNotOwnedVolume: () -> Unit = {},
        onError: () -> Unit = {} // No volume Id or page Id for comparison
    ) {
        if (ownedVolumeId.isNullOrEmpty()) { // Check the owned volume is null or empty or not
            onNotOwnedVolume()
        } else {
            if (volumeId != null && volumeId > 0 && volumeId <= 10) { // Check the volume Id for comparison is null or not
                Log.d(TAG, "Volume ID: $volumeId")
                if (ownedVolumeId.toIntArray().contains(volumeId)) onOwnedVolume() // Check the volume Id is contained on owned volume or not
                else onNotOwnedVolume()
            } else if (pageId != null && pageId > 0 && pageId <= 604) { // Check the page Id for comparison is null or not
                val pagesVolumeId = MainApplication.quranPages[pageId].volumeId // Get page's volume Id
                Log.d(TAG, "Page ID: $pageId")
                Log.d(TAG, "Page's Volume ID: $pagesVolumeId")
                if (ownedVolumeId.toIntArray().contains(pagesVolumeId!!)) onOwnedVolume() // Check the volume Id is contained on owned volume or not
                else onNotOwnedVolume()
            } else onError() // No volume Id or page Id for comparison were entered
        }
    }
}