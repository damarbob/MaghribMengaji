package com.simsinfotekno.maghribmengaji.ui.editprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.jakewharton.processphoenix.ProcessPhoenix
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentEditProfileBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser
import com.simsinfotekno.maghribmengaji.usecase.GetColorFromAttrUseCase

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding

    companion object {
        fun newInstance() = EditProfileFragment()
        private val TAG = "EditProfileFragment"
    }
    /* View models */
    private val viewModel: EditProfileViewModel by viewModels()

    /* Variables */
    private val auth = Firebase.auth

    /* Use cases */
    private val getColorFromAttrUseCase = GetColorFromAttrUseCase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater, container, false)

        val user = auth.currentUser
        val student = MainApplication.studentRepository.getStudent()

        Log.d(TAG, "Student: $student")

        /* Observers */
        viewModel.authUpdateProfileResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess { updatedData ->

                val userId = updatedData["id"] as String

                Log.d(TAG, "authUpdateProfileResult: Updated user profile $userId")
                ProcessPhoenix.triggerRebirth(requireContext())

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "authUpdateProfileResult: ${exception.message}")
            }
        }
        viewModel.editProfileResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.updated_successfully),
                    Toast.LENGTH_SHORT
                ).show()
//                findNavController().popBackStack() // Back to previous page

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        /* Views */
        binding.editProfileInputFullName.setText(user?.displayName)
        binding.editProfileInputAddress.setText(student?.address)
        binding.editProfileInputPhoneNumber.setText(student?.phone)
        binding.editProfileInputNPSN.setText(student?.school)
        binding.editProfileInputLayoutNPSN.helperText =
            "${resources.getString(R.string.npsn_stand_for)}. ${resources.getString(R.string.search_npsn_by_click_search_icon)}"

        if (student?.role == MaghribMengajiUser.ROLE_STUDENT) {
            binding.editProfileInputLayoutBank.visibility = View.GONE
            binding.editProfileInputLayoutAccountNumber.visibility = View.GONE
        }
        binding.editProfileInputBank.setText(student?.bank)
        student?.bankAccount?.let { binding.editProfileInputAccountNumber.setText(it) }

        // Load initial avatar
        Glide
            .with(requireContext())
            .load("https://ui-avatars.com/api/?size=192&name=${user?.displayName?.replace(" ", "-")}&rounded=true&" +
                    "background=${getColorFromAttrUseCase.getColorHex(com.google.android.material.R.attr.colorPrimary, requireContext())}&" +
                    "color=${getColorFromAttrUseCase.getColorHex(com.google.android.material.R.attr.colorOnPrimary, requireContext())}&bold=true")
            .into(binding.editProfileAvatar)
            .clearOnDetach()

        /* Listeners */
        binding.editProfileSaveButton.setOnClickListener {

            // Show confirmation dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.edit_profile))
                .setMessage(getString(R.string.continue_please_ensure_the_data_you_entered_is_correct))
                .setPositiveButton(getString(R.string.send)) { dialog, which ->

                    // Update profile
                    if (user != null) {
                        viewModel.updateProfile(
                            user.uid,
                            hashMapOf(
                                "fullName" to binding.editProfileInputFullName.text.toString(),
                                "address" to binding.editProfileInputAddress.text.toString(),
                                "phone" to binding.editProfileInputPhoneNumber.text.toString(),
                                "school" to binding.editProfileInputNPSN.text.toString(),
                                "bank" to binding.editProfileInputBank.text.toString(),
                                "bankAccount" to binding.editProfileInputAccountNumber.text.toString(),
                            )
                        )
                    }

                }
                .setNeutralButton(getString(R.string.close)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()

        }
        binding.editProfileToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

}