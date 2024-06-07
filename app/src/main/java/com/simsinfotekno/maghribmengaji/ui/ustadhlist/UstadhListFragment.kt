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
import com.simsinfotekno.maghribmengaji.R
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

        /* Views */
        ustadhAdapter = UstadhAdapter(listOf(), findNavController())

        val recyclerView = binding.ustadhListRecyclerViewPage
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ustadhAdapter

        /* Observers */
        ustadhAdapter.selectedUstadh.observe(viewLifecycleOwner) { ustadh ->

            if (ustadh == null)
                return@observe

            binding.ustadhListRecyclerViewPage.visibility = View.GONE
            binding.ustadhListCircularProgress.visibility = View.VISIBLE

            ustadh.id?.let { viewModel.updateUserUstadhId(it) }

        }
        viewModel.updateUstadhIdResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                // Show success message
                Toast.makeText(
                    requireContext(),
                    getString(R.string.ustadh_updated_successfully),
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate to home
                findNavController().navigate(findNavController().graph.startDestinationId)

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.getUstadhResult.observe(viewLifecycleOwner, Observer { result ->
            result?.onSuccess {

                ustadhAdapter.dataSet = it
                ustadhAdapter.notifyDataSetChanged()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        })

        /* Get ustadh list */
        viewModel.getUstadhFromDb()

        return binding.root
    }
}