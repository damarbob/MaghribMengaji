package com.simsinfotekno.maghribmengaji.ui.ustadhvolumeliststudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranVolumeStudentRepository
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhStudentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhVolumeListStudentBinding
import com.simsinfotekno.maghribmengaji.model.QuranVolume
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
        binding = FragmentUstadhVolumeListStudentBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val studentId = arguments?.getString("studentId")

        // Retrieve student page data
        if (studentId != null) {
            viewModel.getStudentPageData(studentId)
        }

        /* Views */
        binding.ustadhVolumeListStudentChat.setEnabled(false) // Disable chat button
        binding.ustadhVolumeListStudentTextNoVolume.visibility = View.GONE // Hide no volume info

        // Toolbar
        binding.ustadhVolumeListAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                // Collapsed
                binding.ustadhVolumeListToolbar.menu.findItem(R.id.menu_student_contact).setVisible(
                    true)
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
                binding.ustadhVolumeListToolbar.menu.findItem(R.id.menu_student_contact).setVisible(
                    false )
            }
        }
        binding.ustadhVolumeListToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_sign_out -> findNavController().popBackStack()
                else -> {return@setOnMenuItemClickListener false}
            }
        }

        // Define the test dataset TODO: Remove if unnecessary
        val testVolumes = List(30) {
            val index = it + 1
            QuranVolume(index, index.toString())
        }

        // Volume dataset are taken from App local variable
        volumeAdapter = VolumeAdapter(
            listOf(),
            findNavController(),
            this
        ) // Set dataset

        pageStudentAdapter = PageStudentAdapter(
            listOf(),
        ) // Set dataset

        val recyclerView = binding.ustadhVolumeListStudentRecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        /* Observers */
        volumeAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe
        }
        viewModel.getStudentProfileResult.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                viewModel.getStudentProfile(studentId!!)
            }
            else  {
                // Use the user variable here
                binding.ustadhVolumeListStudentChat.setEnabled(true) // Enable chat button
                binding.ustadhVolumeListCollapsingToolbarLayout.title = user.fullName // Change toolbar title to the user's full name

                binding.ustadhVolumeListStudentChat.setOnClickListener{
                    if (user.phone != null) { // If phone number exists
                        openWhatsApp(user.phone!!)
                    }
                    else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.error))
                            .setMessage(getString(R.string.user_do_not_have_phone_number))
                            .setPositiveButton(getString(R.string.okay), null)
                            .show()
                    }
                }
            }
        }
        viewModel.getStudentPageResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                // Why when student page result? Because the only data stored in the server is student's page
                // The volume availability follows which pages are available
                // Go to view model to see more
                volumeAdapter.dataSet = ustadhQuranVolumeStudentRepository.getRecords()
                volumeAdapter.notifyDataSetChanged()

                // Handle views
                binding.ustadhVolumeListStudentProgressIndicator.visibility = View.GONE // Hide progress indicator

                // Show info if user has not done any volume yet
                if (ustadhQuranVolumeStudentRepository.getRecordsCount() == 0) {
                    binding.ustadhVolumeListStudentTextNoVolume.visibility = View.VISIBLE // Show no volume info

                    val userName = ustadhStudentRepository.getStudentById(studentId!!)?.fullName // Get user full name once
                    binding.ustadhVolumeListStudentTextNoVolume.text = String.format(getString(R.string.there_are_no_volumes_that_x_has_worked_on_yet), userName)
                }

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        /* Listeners */
        binding.ustadhVolumeListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    fun openWhatsApp(phoneNumber: String) {
        val formattedNumber = phoneNumber.replace("+", "").replace(" ", "")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$formattedNumber")

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }
}