package com.simsinfotekno.maghribmengaji.ui.pagelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageListBinding
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.ui.adapter.PageAdapter
import com.simsinfotekno.maghribmengaji.ui.page.PageViewModel
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import com.simsinfotekno.maghribmengaji.usecase.GetQuranPageRangeString
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStatusCheck

class PageListFragment : Fragment() {

    companion object {
        fun newInstance() = PageListFragment()
    }

    private var _binding: FragmentPageListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PageListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val pageViewModel: PageViewModel by viewModels()

    private lateinit var pageAdapter: PageAdapter

    /* Use case */
    private val quranPageStatusCheck = QuranPageStatusCheck()
    private val getQuranPageRangeString = GetQuranPageRangeString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        // Use case initialization
//        quranPageStatusCheck = QuranPageStatusCheck()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPageListBinding.inflate(inflater, container, false)

        /* Arguments */
        val volumeId = arguments?.getInt("volumeId")
        val pageIds = arguments?.getIntArray("pageIds")!!

        /* Variables */
        val pages = quranPageRepository.getRecordByIds(pageIds)
        val pagesFinished = pages.filter { quranPageStatusCheck(it) == QuranItemStatus.FINISHED }.toMutableList().size

        /* Views */

        // Hide page completed stat
        if (pagesFinished == 0)
            binding.pageListTextPageCompleted.visibility = View.GONE

        // Initialize data set
        pageAdapter = PageAdapter(
            pages,
            findNavController(),
            this
        )

        // Page list
        val recyclerView: RecyclerView = _binding!!.pageListRecyclerViewPage
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = pageAdapter

        // Page info
        binding.pageListTextViewVolume.text = String.format(
            binding.pageListTextViewVolume.context.getString(R.string.volume_x),
            volumeId
        )
        binding.pageListTextPageRange.text = volumeId?.let { getQuranPageRangeString(pageIds.toList(), getString(R.string.page)) }
        binding.pageListTextPageCompleted.text = String.format(getString(R.string.x_pages_completed), pagesFinished)
//        binding.pageListTextViewPageCompletion.text =
//            "0" //TODO: add page completion logic or function
//        binding.pageListTextViewAverageScore.text = "20" //TODO: add average score logic or function

        // Listeners
        binding.pageListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.pageListAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.25

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {
                // Half expanded
                binding.pageListCollapsingToolbarLayout.isTitleEnabled = false
            }
            else if (Math.abs(verticalOffset) >= limitOffset) {
                // Half collapsed
                binding.pageListCollapsingToolbarLayout.isTitleEnabled = true
            }
        }
//        binding.pageListButtonMenu.setOnClickListener {
//            showMenu(it, R.menu.menu_main)
//        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Read quran page from firebase database
    private fun getQuranPage() {

        val db = Firebase.firestore

        Log.d(VolumeListFragment.TAG, "db = $db")

        db.collection("quranPages").get()
            .addOnSuccessListener { documents ->

                val gson = Gson()

                val quranPagesList: List<QuranPage> = documents.map { document ->
                    val data = document.data
                    val json = gson.toJson(data)
                    gson.fromJson(json, QuranPage::class.java)
                }

                Log.d(VolumeListFragment.TAG, "$quranPagesList")

                pageAdapter.dataSet = quranPagesList
                pageAdapter.notifyDataSetChanged()

                quranPageRepository.setRecords(quranPagesList, false)

            }
            .addOnFailureListener { exception ->
                Log.d(VolumeListFragment.TAG, "get failed with ", exception)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_to_fetch_data),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}