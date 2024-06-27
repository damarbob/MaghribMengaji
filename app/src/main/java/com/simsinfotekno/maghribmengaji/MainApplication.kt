package com.simsinfotekno.maghribmengaji

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranRecordingStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranVolumeRepository
import com.simsinfotekno.maghribmengaji.repository.StudentRepository


class MainApplication: Application() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName

        /* Repositories */

        // User repos
        val quranVolumeRepository = QuranVolumeRepository()
        val quranPageRepository = QuranPageRepository()
        val quranPageStudentRepository = QuranPageStudentRepository()
        val studentRepository = StudentRepository()
        val quranRecordingStudentRepository = QuranRecordingStudentRepository()

        // Ustadh repos
        val ustadhStudentRepository = StudentRepository()
        val ustadhQuranVolumeStudentRepository = QuranVolumeRepository()
        val ustadhQuranPageStudentRepository = QuranPageStudentRepository()

        /* Data set preparation */
        // Volume and pages
        val quranVolumes = listOf(
            QuranVolume(1, "1", (1..61).toList()),
            QuranVolume(2, "2", (62..121).toList()),
            QuranVolume(3, "3", (122..181).toList()),
            QuranVolume(4, "4", (182..241).toList()),
            QuranVolume(5, "5", (242..301).toList()),
            QuranVolume(6, "6", (302..361).toList()),
            QuranVolume(7, "7", (362..421).toList()),
            QuranVolume(8, "8", (422..481).toList()),
            QuranVolume(9, "9", (482..541).toList()),
            QuranVolume(10, "10", (542..604).toList()),
        )
        val quranPages = (1..604).map { pageId ->
            // Find the volume for this page
            val volumeId = quranVolumes.find { volume ->
                pageId in volume.pageIds
            }?.id ?: throw IllegalArgumentException("Page $pageId does not belong to any volume")

            QuranPage(pageId, "$pageId", volumeId = volumeId)
        }

    }
    override fun onCreate() {
        super.onCreate()

        // Insert initial data set to repository
        quranVolumeRepository.setRecords(quranVolumes, false)
        quranPageRepository.setRecords(quranPages, false)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val isDarkTheme = (activity.resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
                        activity.window.navigationBarColor =
                            ContextCompat.getColor(
                                activity,
                                com.simsinfotekno.maghribmengaji.R.color.md_theme_surface
                            )
                        activity.window.decorView.getWindowInsetsController()!!
                            .setSystemBarsAppearance(
                                if (isDarkTheme) 0 else WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                            )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26-29
                        activity.window.navigationBarColor =
                            ContextCompat.getColor(
                                activity,
                                com.simsinfotekno.maghribmengaji.R.color.md_theme_surface
                            )
                        activity.window.decorView.systemUiVisibility = if (isDarkTheme) {
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        } else {
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        }
                    } else { // API 21-25
                        activity.window.navigationBarColor =
                            ContextCompat.getColor(
                                activity,
                                com.simsinfotekno.maghribmengaji.R.color.md_theme_surface
                            )
                    }
                }

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }
}