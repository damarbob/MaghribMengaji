package com.simsinfotekno.maghribmengaji

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.simsinfotekno.maghribmengaji.model.QuranChapter
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.model.QuranPageBookmarkStudent
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.repository.QuranChapterRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageBookmarkStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageRepository
import com.simsinfotekno.maghribmengaji.repository.QuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranRecordingStudentRepository
import com.simsinfotekno.maghribmengaji.repository.QuranVolumeRepository
import com.simsinfotekno.maghribmengaji.repository.StudentRepository


class MainApplication : Application() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName

        /* Repositories */

        // User repos
        val quranVolumeRepository = QuranVolumeRepository()
        val quranChapterRepository = QuranChapterRepository()
        val quranPageRepository = QuranPageRepository()
        val quranPageBookmarkStudentRepository = QuranPageBookmarkStudentRepository()
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
            QuranVolume(1, "1", (1..61).toList(), (1..3).toList(), R.drawable.cover_quran_volume_1),
            QuranVolume(2, "2", (62..121).toList(), (3..5).toList(), R.drawable.cover_quran_volume_2),
            QuranVolume(3, "3", (122..181).toList(), (5..8).toList(), R.drawable.cover_quran_volume_3),
            QuranVolume(4, "4", (182..241).toList(), (8..12).toList(), R.drawable.cover_quran_volume_4),
            QuranVolume(5, "5", (242..301).toList(), (12..18).toList(), R.drawable.cover_quran_volume_5),
            QuranVolume(6, "6", (302..361).toList(), (18..25).toList(), R.drawable.cover_quran_volume_6),
            QuranVolume(7, "7", (362..421).toList(), (25..33).toList(), R.drawable.cover_quran_volume_7),
            QuranVolume(8, "8", (422..481).toList(), (33..41).toList(), R.drawable.cover_quran_volume_8),
            QuranVolume(9, "9", (482..541).toList(), (41..57).toList(), R.drawable.cover_quran_volume_9),
            QuranVolume(10, "10", (542..604).toList(), (58..114).toList(), R.drawable.cover_quran_volume_10),
        )
        val quranPages = (1..604).map { pageId ->
            // Find the volume for this page
            val volumeId = quranVolumes.find { volume ->
                pageId in volume.pageIds
            }?.id ?: throw IllegalArgumentException("Page $pageId does not belong to any volume")

            QuranPage(pageId, "$pageId", volumeId = volumeId)
        }

        // Quran chapter
        // List of chapter names in Arabic transliteration
        private val chapterNames = listOf(

            // Volume 1
            "Al-Fatiha",
            "Al-Baqarah",

            "Aal-E-Imran", // Juz 3 & 4 | Volume 1 & 2

            // Volume 2
            "An-Nisa",

            "Al-Ma'idah", // Juz 6 & 7 | Volume 2 & 3

            // Volume 3
            "Al-An'am",
            "Al-A'raf",

            "Al-Anfal", // Juz 9 & 10 | Volume 3 & 4

            // Volume 4
            "At-Tawbah",
            "Yunus",
            "Hud",

            "Yusuf", // Juz 12 & 13 | Volume 4 & 5

            // Volume 5
            "Ar-Ra'd",
            "Ibrahim",
            "Al-Hijr",
            "An-Nahl",
            "Al-Isra",

            "Al-Kahf", // Juz 15 & 16 | Volume 5 & 6

            // Volume 6
            "Maryam",
            "Ta-Ha",
            "Al-Anbiya",
            "Al-Hajj",
            "Al-Mu'minun",
            "An-Nur",

            "Al-Furqan", // Juz 18 & 19 | Volume 6 & 7

            // Volume 7
            "Ash-Shu'ara",
            "An-Naml",
            "Al-Qasas",
            "Al-Ankabut",
            "Ar-Rum",
            "Luqman",
            "As-Sajda",

            "Al-Ahzab", // Juz 21 & 22 | Volume 7 & 8

            // Volume 8
            "Saba",
            "Fatir",
            "Ya-Sin",
            "As-Saffat",
            "Sad",
            "Az-Zumar",
            "Ghafir",

            "Fussilat", // Juz 24 & 25 | Volume 8 & 9

            // Volume 9
            "Ash-Shura",
            "Az-Zukhruf",
            "Ad-Dukhan",
            "Al-Jathiya",
            "Al-Ahqaf",
            "Muhammad",
            "Al-Fath",
            "Al-Hujurat",
            "Qaf",
            "Adh-Dhariyat",
            "At-Tur",
            "An-Najm",
            "Al-Qamar",
            "Ar-Rahman",
            "Al-Waqia",
            "Al-Hadid",

            // Volume 10
            "Al-Mujadila",
            "Al-Hashr",
            "Al-Mumtahina",
            "As-Saff",
            "Al-Jumua",
            "Al-Munafiqun",
            "At-Taghabun",
            "At-Talaq",
            "At-Tahrim",
            "Al-Mulk",
            "Al-Qalam",
            "Al-Haaqqa",
            "Al-Maarij",
            "Nuh",
            "Al-Jinn",
            "Al-Muzzammil",
            "Al-Muddathir",
            "Al-Qiyama",
            "Al-Insan",
            "Al-Mursalat",
            "An-Naba",
            "An-Nazi'at",
            "Abasa",
            "At-Takwir",
            "Al-Infitar",
            "Al-Mutaffifin",
            "Al-Inshiqaq",
            "Al-Buruj",
            "At-Tariq",
            "Al-A'la",
            "Al-Ghashiya",
            "Al-Fajr",
            "Al-Balad",
            "Ash-Shams",
            "Al-Lail",
            "Ad-Duha",
            "Ash-Sharh",
            "At-Tin",
            "Al-Alaq",
            "Al-Qadr",
            "Al-Bayyina",
            "Az-Zalzala",
            "Al-Adiyat",
            "Al-Qaria",
            "At-Takathur",
            "Al-Asr",
            "Al-Humaza",
            "Al-Fil",
            "Quraish",
            "Al-Ma'un",
            "Al-Kawthar",
            "Al-Kafiroon",
            "An-Nasr",
            "Al-Masad",
            "Al-Ikhlas",
            "Al-Falaq",
            "An-Nas",
        )

        // Starting page of each chapter
        val chapterStartPages = listOf(
            1,
            2,
            50,

            77,
            106,

            128,
            151,
            177,

            187,
            208,
            221,
            235,

            249,
            255,
            262,
            267,
            282,
            293,

            305,
            312,
            322,
            332,
            342,
            350,
            359,

            367,
            377,
            385,
            396,
            404,
            411,
            415,
            418,

            428,
            434,
            440,
            446,
            453,
            458,
            467,
            477,

            483,
            489,
            496,
            499,
            502,
            507,
            511,
            515,
            518,
            520,
            523,
            526,
            528,
            531,
            534,
            537,

            542,
            545,
            549,
            551,
            553,
            554,
            556,
            558,
            560,
            562,
            564,
            566,
            568,
            570,
            572,
            574,
            575,
            577,
            578,
            580,
            582,
            583,
            585,
            586,
            587,
            587,
            589,
            590,
            591,
            591,
            592,
            593,
            594,
            595,
            595,
            596,
            596,
            597,
            597,
            598,
            598,
            599,
            599,
            600,
            600,
            601,
            601,
            601,
            602,
            602,
            602,
            603,
            603,
            603,
            604,
            604,
            604,
        )

        // Calculate end pages by shifting start pages and adding an ending point
        val chapterEndPages = (chapterStartPages.drop(1) + 604).toMutableList()

        ///////////////////////////

        // Start from top
        // List of chapters that starts from the very top of the page
        // Juz 30: 78, 80, 82, 86, 103, 106, 109, 112
        private val startFromTopChapterIds = mutableListOf(
            // Volume 1
            1, 2, 3,

            // Volume 2
            4,

            // Volume 3
            6, 7, 8,

            // Volume 4
            9, 10,

            // Volume 5
            13, 15, 17,

            // Volume 6
            19, 21, 22, 23, 24,

            // Volume 7
            26, 27, 31, 32, 33,

            // Volume 8
            34, 37, 38, 41,

            // Volume 9
            42, 44, 45, 47, 48, 50, 53,

            // Volume 10
            58, 60, 62, 64, 65, 66, 67, 72, 73, 78, 80, 82, 86, 103, 106, 109, 112)

        //////////////////////////

        // Create QuranChapters
        val quranChapters =
            chapterStartPages.zip(chapterEndPages).mapIndexed { index, (startPage, endPage) ->

                // The previous chapter of the chapter that starts from top
                val startFromTopPreviousChapterIds = mutableListOf<Int>()
                startFromTopChapterIds.forEach {
                    startFromTopPreviousChapterIds.add(it - 1)
                }

                // Determine page IDs for the chapter
                val pageIds = if (startPage == endPage) {
                    // Chapters that start and end on the same page
                    listOf(startPage)
                } else {
                    if ((index + 1) in startFromTopPreviousChapterIds) {
                        // Chapters that start from top
                        (startPage until endPage).toList()
                    } else {
                        // Chapters that start from middle
                        (startPage until endPage + 1).toList()
                    }
                }

                // Find volume IDs
                val volumeIds = quranVolumes.filter { volume ->
                    volume.pageIds.any { it in pageIds }
                }.map { it.id }

                // Create QuranChapter
                QuranChapter(
                    id = index + 1,
                    name = chapterNames[index], // Use the chapter name from the list
                    volumeIds = volumeIds,
                    pageIds = pageIds
                )
            }

        // Bookmarks
        val pageOffsets = listOf(
            1, 22, 42, 62, 82, 102, 122, 142, 162, 182,
            202, 222, 242, 262, 282, 302, 322, 342, 362, 382,
            402, 422, 442, 462, 482, 502, 522, 542, 562, 582
        )
        val quranPageBookmarksStudent = (1..30).mapIndexed { index, juzId ->
            QuranPageBookmarkStudent(pageOffsets[index], "$juzId")
        }

    }

    override fun onCreate() {
        super.onCreate()

        // Modify data
        chapterEndPages.forEachIndexed { index, it ->
            val chapterNumber = (index + 1)

            if (chapterNumber == 78 || chapterNumber == 80 || chapterNumber == 82 || chapterNumber == 86 || chapterNumber == 103 ||
                chapterNumber == 106 || chapterNumber == 109
            ) {
                chapterEndPages[chapterNumber - 1] = chapterEndPages[chapterNumber - 1] - 1
            }

        }

        // Insert initial data set to repository
        quranVolumeRepository.setRecords(quranVolumes, false)
        quranChapterRepository.setRecords(quranChapters, false)
        quranChapterRepository.getRecords().forEach {
            Log.d(
                TAG,
                "${it.id}. ${it.name}: Page ${chapterStartPages[it.id - 1]} - ${chapterEndPages[it.id - 1]} | Page ids: ${it.pageIds}"
            )
        }
        quranPageRepository.setRecords(quranPages, false)
        quranPageBookmarkStudentRepository.setRecords(quranPageBookmarksStudent, false)

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