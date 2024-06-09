package com.simsinfotekno.maghribmengaji.ui.ustadhvolumeliststudent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranVolumeStudentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhVolumeListStudentBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.PageStudentAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter

class UstadhVolumeListStudentFragment : Fragment() {

    private lateinit var binding: FragmentUstadhVolumeListStudentBinding

    companion object {
        fun newInstance() = UstadhVolumeListStudentFragment()
    }

    private val viewModel: UstadhVolumeListStudentViewModel by viewModels()

    /* Views */
    private lateinit var volumeAdapter: VolumeAdapter
    private lateinit var pageStudentAdapter: PageStudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUstadhVolumeListStudentBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val studentId = arguments?.getString("studentId")

        // Retrieve student page data
        if (studentId != null) {
            viewModel.getStudentPageData(studentId)
        }

        /* Views */
        // Volume dataset are taken from App local variable
        volumeAdapter = VolumeAdapter(
            listOf(),
            VolumeAdapter.VIEW_ITEM_LIST
        ) // Set dataset

        pageStudentAdapter = PageStudentAdapter(
            listOf(),
        ) // Set dataset

        val recyclerView = binding.studentVolumeListRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        /* Observers */
        volumeAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe

            // Navigate to student page list and pass volume id
            val bundle = Bundle()
            bundle.putInt("volumeId", it.id)
            findNavController().navigate(
                R.id.action_studentVolumeListFragment_to_studentPageListFragment,
                bundle
            )

        }
        viewModel.getStudentPageResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                // Why when student page result? Because the only data stored in the server is student's page
                // The volume availability follows which pages are available
                // Go to view model to see more
                volumeAdapter.dataSet = ustadhQuranVolumeStudentRepository.getRecords()
                volumeAdapter.notifyDataSetChanged()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}