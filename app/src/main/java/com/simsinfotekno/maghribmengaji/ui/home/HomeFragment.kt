package com.simsinfotekno.maghribmengaji.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranChapterRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageBookmarkStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentHomeBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnMainActivityFeatureRequest
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.adapter.BannerAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.ChapterAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.PageBookmarkStudentAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeByStatus
import com.simsinfotekno.maghribmengaji.usecase.NetworkConnectivityUseCase
import com.simsinfotekno.maghribmengaji.usecase.OpenWhatsApp
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck
import com.simsinfotekno.maghribmengaji.usecase.ShowPopupMenu
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : Fragment() {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
    }

//    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var binding: FragmentHomeBinding

    /* View models */
    private val viewModel: HomeViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    // Adapters
    private lateinit var volumeAdapter: VolumeAdapter
    private lateinit var pageBookmarkStudentAdapter: PageBookmarkStudentAdapter
    private lateinit var chapterAdapter: ChapterAdapter

    // Use case
    private val showPopupMenu: ShowPopupMenu = ShowPopupMenu()
    private val getQuranVolumeByStatus = GetQuranVolumeByStatus()
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()
    private val openWhatsApp = OpenWhatsApp()
    private lateinit var networkConnectivityUseCase: NetworkConnectivityUseCase

    // Handlers
    private lateinit var autoScrollHandler: Handler
    private lateinit var autoScrollRunnable: Runnable

    // Variables
    private var ustadhPhoneNumber: String? = null
    private var ustadhName: String? = null
    private var volumeInProgress: List<QuranVolume>? = null
    private var materialAlertDialog: AlertDialog? = null
    private var connectionStatus: ConnectivityObserver.Status =
        ConnectivityObserver.Status.Unavailable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        materialAlertDialog = MaterialAlertDialogBuilder(requireContext()).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Check connection
//        networkConnectivityUseCase = context?.let { NetworkConnectivityUseCase(it) }!!
//        checkConnection()

        /* Views */

        binding.homeLayoutInProgress.visibility = View.GONE
        binding.homeLayoutNoProgress.visibility = View.GONE
        binding.homeFabVolumeList.visibility = View.GONE
        binding.homeLayoutNoNetwork.visibility = View.GONE
        binding.homeLayoutJuzList.visibility = View.GONE

        binding.homeTextTitle.text =
            "${Firebase.auth.currentUser?.displayName}" // ${getString(R.string.salam)},
                ?: getString(R.string.app_name)

        // Volume adapter
        // Set volume dataset with status on progress
        volumeAdapter = VolumeAdapter(
            getQuranVolumeByStatus.invoke(QuranItemStatus.ON_PROGRESS),
            findNavController(),
            this
        )

        // Bookmark adapter
        pageBookmarkStudentAdapter = PageBookmarkStudentAdapter(
            quranPageBookmarkStudentRepository.getRecords(),
            findNavController()
        )

        // Chapter adapter
        chapterAdapter = ChapterAdapter(
            quranChapterRepository.getRecords(),
            findNavController(),
        )

        // Volume list
        val recyclerView: RecyclerView = binding.homeRecyclerViewVolume
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        // Bookmark list
        val recyclerViewBookmark: RecyclerView = binding.homeRecyclerViewJuzBookmarks
        recyclerViewBookmark.layoutManager =
            GridLayoutManager(context, 3)
        recyclerViewBookmark.adapter = pageBookmarkStudentAdapter

        // Chapter list
//        val chapterRecyclerView: RecyclerView = binding.homeRecyclerViewChapters
//        chapterRecyclerView.layoutManager =
//            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        chapterRecyclerView.adapter = chapterAdapter

//        binding.homeTextLastWritten.text = String.format(requireContext().getString(R.string.quran_page), MainActivity.student.lastPageId) // Last written

        // Progress indicator
        val allPagesCount = 604
        val finishedQuranPageStudent =
            quranPageStudentRepository.getPagesByStatus(QuranItemStatus.FINISHED)
        val progressPercentage = finishedQuranPageStudent.size / allPagesCount * 100
        binding.homeTextPagePercentage.text = (progressPercentage).toString()
//        binding.homeProgressIndicatorPagePercentage.progress = if (progressPercentage < 5) 5 else progressPercentage
        binding.homeProgressIndicatorPagePercentage.progress = progressPercentage

        // Banner
        setupBanner()

        /* Observers */
        volumeAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null) return@observe
        }
        viewModel.lastPageId.observe(viewLifecycleOwner) { lastPageId ->
            if (lastPageId == null) {
                binding.homeTextLastWritten.text = getString(R.string.no_data)
                binding.homeTextLastWrittenVolume.text = getString(R.string.no_data)
            } else {
                val lastVolume = quranVolumeRepository.getRecordByPageId(lastPageId) // Get last volume based on lastPageId

                binding.homeTextLastWritten.text = String.format(
                    requireContext().getString(R.string.page_x),
                    lastPageId.toString()
                )
                binding.homeTextLastWrittenVolume.text = String.format(
                    requireContext().getString(R.string.volume_x),
                    lastVolume?.name
                )
                binding.homeTextLastWritten.setOnClickListener {

                    // Bundle for the page fragment
                    val bundle = Bundle().apply {
                        putInt("pageId", lastPageId)
                    }

                    // Navigate to PageFragment
                    findNavController().navigate(R.id.action_global_pageFragment, bundle)
                }
                binding.homeTextLastWrittenVolume.setOnClickListener {


                    // Bundle for page list fragment
                    val bundle = Bundle().apply {
                        lastVolume?.id?.let { volumeId ->
                            putInt("volumeId", volumeId)
                            putIntArray("pageIds", lastVolume.pageIds.toIntArray())
                        }

                    }

                    // Navigate to PageFragment
                    findNavController().navigate(R.id.action_global_pageListFragment, bundle)

                }
            }
        }
        viewModel.volumeInProgressDataSet.observe(viewLifecycleOwner) { data ->
            volumeInProgress = data
            if (data == null) {
                setHomeUI()
                return@observe
            }

            setHomeUI()
            volumeAdapter.dataSet = data
            volumeAdapter.notifyDataSetChanged()
            Log.d(TAG, "Added ${data.size} records to volume adapter")
//            binding.homeRecyclerViewVolume.visibility = View.VISIBLE
            volumeInProgress = data
        }
        studentRepository.ustadhLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "Ustadh: $it")
            binding.homeTextUstadhName.text =
                if (it != null) it.fullName else getString(R.string.no_data)
            if (it != null) {
                ustadhPhoneNumber = if (it.phone != null) it.phone else null
                ustadhName = it.fullName
            }
        }
        mainViewModel.connectionStatus.observe(viewLifecycleOwner) {
            Log.d("HomeFragment connection", connectionStatus.toString())
            Log.d(TAG, it.toString())
            connectionStatus = when (it) {
                ConnectivityObserver.Status.Available -> ConnectivityObserver.Status.Available
                else -> {
                    ConnectivityObserver.Status.Unavailable
                }
            }
            if (it != ConnectivityObserver.Status.Available) {
                binding.homeProgressIndicatorLoading.visibility = View.GONE
                binding.homeLayoutNoNetwork.visibility = View.VISIBLE
            }
            setHomeUI()
        }
//        viewModel.progressVisibility.observe(viewLifecycleOwner) {
//            binding.homeProgressIndicatorLoading.visibility = if (it) View.VISIBLE else View.GONE
//            binding.homeFabVolumeList.visibility = if (it) View.GONE else View.VISIBLE
//            if (volumeInProgress != null) {
//                if (volumeInProgress!!.isNotEmpty()) {
//                    binding.homeLayoutInProgress.visibility = if (it) View.GONE else View.VISIBLE
//                } else binding.homeLayoutNoProgress.visibility = if (it) View.VISIBLE else View.GONE
//            }
//        }

        /* Listeners */
        binding.homeToolbar.setNavigationOnClickListener {
            EventBus.getDefault().post(
                OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.OPEN_DRAWER)
            )
        }
        binding.homeAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.25

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {

                // Half expanded
                binding.homeCollapsingToolbarLayout.isTitleEnabled = false
                binding.homeToolbar.setNavigationIconTint(
                    getColorFromAttr(
                        com.google.android.material.R.attr.colorOnPrimary,
                        requireContext()
                    )
                )

            } else if (Math.abs(verticalOffset) >= limitOffset) {

                // Half collapsed
                binding.homeCollapsingToolbarLayout.isTitleEnabled = true
                binding.homeToolbar.setNavigationIconTint(
                    getColorFromAttr(
                        com.google.android.material.R.attr.colorPrimary,
                        requireContext()
                    )
                )

            }
        }
        binding.homeTextUstadhName.setOnClickListener {
            if (ustadhName != null) { // Check if user has Ustadh
                if (ustadhPhoneNumber != null) { // Check if Ustadh has phone number
                    materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.chat_with_ustadh))
                        .setMessage(getString(R.string.you_will_chat_with_ustadh, ustadhName))
                        .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                            openWhatsApp(requireContext(), ustadhPhoneNumber!!)
                        }
                        .show()
                } else { // Ustadh has'nt phone number
                    materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.chat_with_ustadh))
                        .setMessage(
                            getString(
                                R.string.your_ustadh_hasnt_entered_his_whatsapp,
                                ustadhName
                            )
                        )
                        .setPositiveButton(getString(R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else {
                materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.chat_with_ustadh))
                    .setMessage(getString(R.string.you_dont_have_ustadh))
                    .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        binding.homeButtonStartJourney.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_volumeListFragment)
        }
        binding.homeFabVolumeList.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_volumeListFragment)
        }

        return binding.root
    }

//    private fun checkConnection() {
//        networkConnectivityUseCase(viewLifecycleOwner, onAvailableNetwork = {
////            setHomeUI(true)
//            connectionStatus = ConnectivityObserver.Status.Available
//        }, onUnavailableNetwork = {
//            connectionStatus = ConnectivityObserver.Status.Unavailable
////            setHomeUI(false)
//        })
//    }

    private fun setHomeUI() {
        val materialFade = MaterialFade().apply {
            duration = 150L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
        Log.d(TAG, "$volumeInProgress")

        binding.homeProgressIndicatorLoading.visibility = View.VISIBLE
        binding.homeLayoutJuzList.visibility = View.VISIBLE

        if (volumeInProgress != null) {

            binding.homeProgressIndicatorLoading.visibility = View.GONE
            binding.homeLayoutNoNetwork.visibility = View.GONE
            binding.homeFabVolumeList.visibility = View.VISIBLE

            Log.d(TAG, "volume in progress is not null")

            if (volumeInProgress!!.isNotEmpty()) {
                Log.d(TAG, "volume in progress is not empty")
                binding.homeLayoutInProgress.visibility = View.VISIBLE
                binding.homeLayoutNoProgress.visibility = View.GONE
            } else {
                Log.d(TAG, "volume in progress is empty")
                binding.homeLayoutInProgress.visibility = View.GONE
                binding.homeLayoutNoProgress.visibility = View.VISIBLE
            }

        } else {
            Log.d(TAG, "volume in progress is null")
        }

//        if (connectionStatus != ConnectivityObserver.Status.Available) {
//            binding.homeProgressIndicatorLoading.visibility = View.GONE
//            binding.homeLayoutNoNetwork.visibility = View.VISIBLE
//        }
    }

    private fun setupBanner() {
        val bannerList = ArrayList<Int>()
        bannerList.apply {
            add(R.drawable.banner_1)
            add(R.drawable.banner_2)
            add(R.drawable.banner_3)
        }

        binding.homeViewPagerBanner.apply {
            adapter = BannerAdapter(bannerList)
            currentItem =
                1 // setting the current item of the infinite ViewPager to the actual first element
        }

        // function for registering a callback to update the ViewPager
        // and provide a smooth flow for infinite scroll
        onInfinitePageChangeCallback(bannerList.size + 2)
        setupAutoScroll()
    }

    private fun onInfinitePageChangeCallback(itemSize: Int) {
        binding.homeViewPagerBanner.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (binding.homeViewPagerBanner.currentItem) {
                        itemSize - 1 -> binding.homeViewPagerBanner.setCurrentItem(1, false)
                        0 -> binding.homeViewPagerBanner.setCurrentItem(itemSize - 2, false)
                    }

                    autoScrollHandler.postDelayed(
                        autoScrollRunnable,
                        3000
                    ) // Restart auto-scroll when the state is idle
                } else autoScrollHandler.removeCallbacks(autoScrollRunnable) // Pause auto-scroll when not idle
            }
        })
    }

    private fun setupAutoScroll() {
        autoScrollHandler = Handler(Looper.getMainLooper())
        autoScrollRunnable = object : Runnable {
            override fun run() {
                binding.homeViewPagerBanner.currentItem += 1
                autoScrollHandler.postDelayed(this, 3000) // Adjust the delay time as needed
            }
        }
//        autoScrollHandler.postDelayed(
//            autoScrollRunnable,
//            3000
//        ) // Initial delay before starting auto-scroll
    }

    override fun onPause() {
        super.onPause()
        autoScrollHandler.removeCallbacks(autoScrollRunnable) // Stop auto-scroll when the fragment/activity is not visible
    }

    override fun onResume() {
        super.onResume()
        autoScrollHandler.postDelayed(
            autoScrollRunnable,
            3000
        ) // Restart auto-scroll when the fragment/activity becomes visible again
//        checkConnection()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun _105606032024(event: OnUserDataLoaded) {
        if (event.userDataEvent == UserDataEvent.PAGE) {

        }
    }

    // TODO: Move to use case
    @ColorInt
    private fun getColorFromAttr(attr: Int, context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(attr, tv, true)
        return context.resources.getColor(tv.resourceId)
    }

}