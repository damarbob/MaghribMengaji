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
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumeRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhPageListStudentBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.PageStudentAdapter

class UstadhPageListStudentFragment : Fragment() {

    private lateinit var binding: FragmentUstadhPageListStudentBinding

    companion object {
        private val TAG = UstadhPageListStudentFragment::class.java.simpleName
        fun newInstance() = UstadhPageListStudentFragment()
    }

    private val viewModel: UstadhPageListStudentViewModel by viewModels()

    /* Views */
    private lateinit var pageStudentAdapter: PageStudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUstadhPageListStudentBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val volumeId = arguments?.getInt("volumeId")

        Log.d(TAG, " Volume: ${quranVolumeRepository.getRecordById(volumeId)?.pageIds?.toIntArray().toString()}")

        /* Variables */
        val pages = quranVolumeRepository.getRecordById(volumeId)?.pageIds?.toIntArray()?.let {
            ustadhQuranPageStudentRepository.getRecordByIdsNoStrict(it)
        } ?: listOf() // Show pages within the volume otherwise show nothing

        /* Views */
        pageStudentAdapter = PageStudentAdapter(
            pages,
        ) // Set dataset

        val recyclerView = binding.studentPageListRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = pageStudentAdapter

        /* Observers */
        pageStudentAdapter.selectedPageStudent.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            // Navigate to scoring fragment and pass page id
            val bundle = Bundle()
            it.pageId?.let { it1 -> bundle.putInt("pageId", it1) }
            findNavController().navigate(R.id.action_studentPageListFragment_to_ustadhScoringFragment, bundle)

        }

        return binding.root
    }
}