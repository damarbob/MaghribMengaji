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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.ActivityRestartable
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhListBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.ui.adapter.UstadhAdapter
import com.simsinfotekno.maghribmengaji.usecase.TransactionService


class UstadhListFragment : Fragment() {

    lateinit var binding: FragmentUstadhListBinding

    companion object {
        fun newInstance() = UstadhListFragment()
    }

    private val viewModel: UstadhListViewModel by viewModels()

    // Variables
    private lateinit var ustadhAdapter: UstadhAdapter

    /* Use cases */
    private val transactionService = TransactionService()

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

            // Show confirmation dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.choose_ustadh))
                .setMessage("${getString(R.string.choose_ustadh)} ${ustadh.fullName}. ${getString(R.string.are_you_sure)}")
                .setPositiveButton(getString(R.string.send)) { dialog, which ->

                    // Adjust view
                    binding.ustadhListRecyclerViewPage.visibility = View.GONE
                    binding.ustadhListCircularProgress.visibility = View.VISIBLE

                    // Set ustadh
                    ustadh.id?.let { viewModel.updateUserUstadhId(it) }

                }
                .setNeutralButton(getString(R.string.close)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()

        }
        viewModel.updateUstadhIdResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess { ustadhId ->

                // Deposit reward to teacher for having a new student
                transactionService.depositTo(ustadhId, MaghribMengajiUser.TEACHER_REWARD) {
                    it.onSuccess {

                        // Send a notification to the teacher
                        //viewModel.sendNotificationToUstadh(ustadhId)

                        // Show success message
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.ustadh_updated_successfully),
                            Toast.LENGTH_SHORT
                        ).show()

                        (activity as? ActivityRestartable)?.restartActivity() // Restart activity

                        // Navigate to home
                        findNavController().navigate(R.id.action_ustadhListFragment_to_homeFragment)

                    }
                    it.onFailure { e ->
                        Toast.makeText(
                            requireContext(),
                            e.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

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