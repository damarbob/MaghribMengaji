package com.simsinfotekno.maghribmengaji.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.LoginActivity
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentHomeBinding
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.event.OnUserDataLoaded
import com.simsinfotekno.maghribmengaji.ui.adapter.BannerAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeByStatus
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

    private var _binding: FragmentHomeBinding? = null

    /* View model */
    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Views
    private lateinit var volumeAdapter: VolumeAdapter

    // Use case
    private val showPopupMenu: ShowPopupMenu = ShowPopupMenu()
    private val getQuranVolumeByStatus = GetQuranVolumeByStatus()
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()
    private val openWhatsApp = OpenWhatsApp()

    // Handlers
    private lateinit var autoScrollHandler: Handler
    private lateinit var autoScrollRunnable: Runnable

    // Variables
    private var ustadhPhoneNumber: String? = null
    private var ustadhName: String? = null
    private var materialAlertDialog: AlertDialog? = null

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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /* Views */
        binding.homeTextTitle.text = "${Firebase.auth.currentUser?.displayName}" // ${getString(R.string.salam)},
            ?: getString(R.string.app_name)

        // Volume adapter
        // Set volume dataset with status on progress
        volumeAdapter = VolumeAdapter(
            getQuranVolumeByStatus.invoke(QuranItemStatus.ON_PROGRESS),
            findNavController(),
            this
        )

        // Volume list
        val recyclerView: RecyclerView = binding.homeRecyclerViewVolume
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

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

            // Navigate to page list fragment and passing volume id
//            val bundle = Bundle()
//            bundle.putInt("volumeId", it.id)
//            bundle.putIntArray("pageIds", it.pageIds.toIntArray())
//            findNavController().navigate(R.id.action_homeFragment_to_pageListFragment, bundle)

        }
        viewModel.lastPageId.observe(viewLifecycleOwner) { lastPageId ->
            if (lastPageId == null) {
                binding.homeTextLastWritten.text = getString(R.string.no_data)
                binding.homeTextLastWrittenVolume.text = getString(R.string.no_data)
            } else {
                binding.homeTextLastWritten.text = String.format(
                    requireContext().getString(R.string.page_x),
                    lastPageId.toString()
                )
                binding.homeTextLastWrittenVolume.text = String.format(
                    requireContext().getString(R.string.volume_x),
                    quranVolumeRepository.getRecordByPageId(lastPageId)?.name
                )
            }
        }
        viewModel.volumeInProgressDataSet.observe(viewLifecycleOwner) { data ->
            // TODO: volume in progress visibility
            if (data == null) {
                return@observe
            }
            else if (data.isEmpty()) {
                binding.homeLayoutNoProgress.visibility = View.VISIBLE
                binding.homeLayoutInProgress.visibility = View.GONE
                return@observe
            }

            volumeAdapter.dataSet = data
            volumeAdapter.notifyDataSetChanged()
            Log.d(TAG, "Added ${data.size} records to volume adapter")
            val materialFade = MaterialFade().apply {
                duration = 150L
            }
            TransitionManager.beginDelayedTransition(binding.root, materialFade)
            binding.homeRecyclerViewVolume.visibility = View.VISIBLE
            binding.homeLayoutInProgress.visibility = View.VISIBLE
            binding.homeLayoutNoProgress.visibility = View.GONE
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

        /* Listeners */
        binding.homeToolbar.setNavigationOnClickListener {
            showPopupMenu(requireContext(), it, R.menu.menu_main) {
                when (it.itemId) {
                    R.id.menu_profile -> findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
                    R.id.menu_sign_out -> {

                        // Show confirmation dialog
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setMessage(getString(R.string.you_will_be_logged_out_from_this_account))
                            .setNeutralButton(resources.getString(R.string.close)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                                // Logout and navigate to login
                                mainViewModel.logout()
                                navigateToLoginActivity()
                            }
                            .show()

                    }
                }
                return@showPopupMenu false
            }
        }
        binding.homeAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.25

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {
                // Half expanded
                binding.homeCollapsingToolbarLayout.isTitleEnabled = false
            } else if (Math.abs(verticalOffset) >= limitOffset) {
                // Half collapsed
                binding.homeCollapsingToolbarLayout.isTitleEnabled = true
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

        return root
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
                }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        EventBus.getDefault().unregister(this)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Optional: Finish MainActivity to prevent the user from coming back to it
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun _105606032024(event: OnUserDataLoaded) {
        if (event.userDataEvent == UserDataEvent.PAGE) {

        }
    }

}