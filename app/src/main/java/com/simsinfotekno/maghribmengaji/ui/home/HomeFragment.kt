package com.simsinfotekno.maghribmengaji.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
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
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeByStatus
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /* Views */
        binding.homeTextTitle.text = Firebase.auth.currentUser?.displayName
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
            }
            else {
                binding.homeTextLastWritten.text = String.format(requireContext().getString(R.string.page_x), lastPageId.toString())
                binding.homeTextLastWrittenVolume.text = String.format(requireContext().getString(R.string.volume_x), quranVolumeRepository.getRecordByPageId(lastPageId)?.name)
            }
        }
        viewModel.volumeInProgressDataSet.observe(viewLifecycleOwner) { data ->

            if (data == null)
                return@observe

            volumeAdapter.dataSet = data
            volumeAdapter.notifyDataSetChanged()
            Log.d(TAG, "Added dataset to volume adapter")
            val materialFade = MaterialFade().apply {
                duration = 150L
            }
            TransitionManager.beginDelayedTransition(binding.root, materialFade)
            binding.homeRecyclerViewVolume.visibility = View.VISIBLE
        }
        studentRepository.ustadhLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "Ustadh: $it")
            binding.homeTextUstadhName.text =
                if (it != null) it.fullName else getString(R.string.no_data)
        }

        /* Listeners */
        binding.homeButtonMenu.setOnClickListener {
            showPopupMenu(requireContext(), it, R.menu.menu_main) {
                when (it.itemId) {
                    R.id.menu_profile -> findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
                    R.id.menu_sign_out -> {
                        mainViewModel.logout()
                        navigateToLoginActivity()
                    }
                }
                return@showPopupMenu false
            }
        }

        return root
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