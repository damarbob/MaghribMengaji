package com.simsinfotekno.maghribmengaji.ui.ustadhpageliststudent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhPageListStudentBinding
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.ui.adapter.PageStudentAdapter
import com.simsinfotekno.maghribmengaji.usecase.GetQuranPageRangeString

class UstadhPageListStudentFragment : Fragment() {

    private lateinit var binding: FragmentUstadhPageListStudentBinding

    companion object {
        private val TAG = UstadhPageListStudentFragment::class.java.simpleName
        fun newInstance() = UstadhPageListStudentFragment()
    }

    private val viewModel: UstadhPageListStudentViewModel by viewModels()

    /* Views */
    private lateinit var pageStudentAdapter: PageStudentAdapter

    // Use case
    private val getQuranPageRangeString = GetQuranPageRangeString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUstadhPageListStudentBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val volumeId = arguments?.getInt("volumeId")

        Log.d(
            TAG,
            " Volume: ${
                quranVolumeRepository.getRecordById(volumeId)?.pageIds?.toIntArray().toString()
            }"
        )

        /* Variables */
        viewModel.init(volumeId ?: viewModel.volumeId ?: 0) //

        /* Views */

        // Recycler view
        // Define the test dataset TODO: Remove if unnecessary
        val testPages = List(30) {
            val index = it + 1
            QuranPageStudent(index, index.toString())
        }

        pageStudentAdapter = PageStudentAdapter(
            listOf(),
        ) // Set dataset

        val recyclerView = binding.studentPageListRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = pageStudentAdapter

        binding.ustadhPageListStudentTextVolume.text =
            getString(R.string.volume_x, volumeId.toString())
        binding.ustadhPageListStudentCollapsingToolbarLayout.title = binding.ustadhPageListStudentTextVolume.text
        binding.ustadhPageListStudentTextPageRange.text = volumeId?.let { getQuranPageRangeString(it, getString(R.string.page)) }

        binding.ustadhPageListStudentTab.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {
                    0 -> viewModel.getAll()
                    1 -> viewModel.getOnProgress()
                    2 -> viewModel.getFinished()
                    else -> Log.d(TAG, "Unknown tab selected")
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        /* Observers */
        viewModel._pagesLiveData.observe(viewLifecycleOwner) {
            pageStudentAdapter.dataSet = it
            pageStudentAdapter.notifyDataSetChanged()
        }
        pageStudentAdapter.selectedPageStudent.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            // Navigate to scoring fragment and pass page id
            val bundle = Bundle()
            it.pageId?.let { it1 -> bundle.putInt("pageId", it1) }
            findNavController().navigate(
                R.id.action_studentPageListFragment_to_ustadhScoringFragment,
                bundle
            )

        }

        /* Listeners */
        binding.ustadhPageListStudentToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.ustadhPageListStudentAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.25

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {
                // Half expanded
                binding.ustadhPageListStudentCollapsingToolbarLayout.isTitleEnabled = false
            }
            else if (Math.abs(verticalOffset) >= limitOffset) {
                // Half collapsed
                binding.ustadhPageListStudentCollapsingToolbarLayout.isTitleEnabled = true
            }
        }

        return binding.root
    }
}