package com.simsinfotekno.maghribmengaji.ui.ustadhlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhListBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.UstadhAdapter

class UstadhListFragment : Fragment() {

    lateinit var binding: FragmentUstadhListBinding

    companion object {
        fun newInstance() = UstadhListFragment()
    }

    private val viewModel: UstadhListViewModel by viewModels()

    // Variables
    private lateinit var ustadhAdapter: UstadhAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUstadhListBinding.inflate(layoutInflater)

        /* Observers */
        viewModel.getUstadhResult.observe(viewLifecycleOwner, Observer { result ->
            result?.onSuccess {

                ustadhAdapter.dataSet = it
                ustadhAdapter.notifyDataSetChanged()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        })

        /* Views */
        ustadhAdapter = UstadhAdapter(listOf(), findNavController())

        val recyclerView = binding.ustadhListRecyclerViewPage
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ustadhAdapter

        /* Get ustadh list */
        viewModel.getUstadhFromDb()

        return binding.root
    }
}