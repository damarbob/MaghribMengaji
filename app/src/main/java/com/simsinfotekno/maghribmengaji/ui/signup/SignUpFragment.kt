package com.simsinfotekno.maghribmengaji.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSignUpBinding
import com.simsinfotekno.maghribmengaji.model.CountryCode


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        /* Observers */
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {
                // Navigate to the next screen or update UI
                Toast.makeText(
                    requireContext(),
                    getString(R.string.sign_up_successful),
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()  // Finish the current activity so the user can't navigate back to the login screen

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        setUpCountryCodeSpinner()

        /* Listeners */
        binding.signUpButton.setOnClickListener {
            val displayName = binding.signUpInputFullName.text.toString()
            val email = binding.signUpInputEmailAddress.text.toString()
            val selectedCountryCode = binding.signUpSpinnerCountryCode.selectedItem as CountryCode
            var phone = binding.signUpInputPhoneNumber.text.toString()
            val password = binding.signUpInputPassword.text.toString()

            // Strip leading zeros
            while (phone.startsWith("0")) {
                phone = phone.substring(1)
            }

            if (phone.isNotEmpty()) {
                val fullPhoneNumber = "${selectedCountryCode.code}$phone"
//                Toast.makeText(requireContext(), "Phone Number: $fullPhoneNumber", Toast.LENGTH_LONG).show()
            } else {
                binding.signUpTextInputLayoutPhone.error = resources.getString(R.string.please_enter_valid_phone)
            }

            // Validate the inputs
            when {
                displayName.isEmpty() -> {
                    showErrorDialog(resources.getString(R.string.full_name_is_required))
                }

                email.isEmpty() -> {
                    showErrorDialog(resources.getString(R.string.email_is_required))
                }

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showErrorDialog(resources.getString(R.string.invalid_email_format))
                }

                password.isEmpty() -> {
                    showErrorDialog(resources.getString(R.string.password_is_required))
                }

                password.length < 6 -> { // Example of password length validation
                    showErrorDialog(resources.getString(R.string.password_must_be_at_least_6_characters_long))
                }

                !android.util.Patterns.PHONE.matcher(phone).matches() -> {
                    showErrorDialog(resources.getString(R.string.invalid_phone))
                }

                else -> {
                    // All validations passed, proceed to sign up confirmation

                    // Show confirmation dialog
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.auth_signup))
                        .setMessage(getString(R.string.continue_please_ensure_the_data_you_entered_is_correct))
                        .setPositiveButton(getString(R.string.send)){ dialog, which ->
                            viewModel.signUpWithEmailPassword(displayName, email, phone, password)
                        }
                        .setNeutralButton(getString(R.string.close)){ dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        binding.signUpLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        return binding.root
    }

    private fun setUpCountryCodeSpinner() {
        val countryCodes = listOf(
            CountryCode("+62","ID"),
            CountryCode("+1", "USA"),
            CountryCode("+44", "UK"),
            CountryCode("+91", "India"),
            // Add more country codes as needed
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countryCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.signUpSpinnerCountryCode.adapter = adapter
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.error))
            .setMessage(message)
            .setNeutralButton(resources.getString(R.string.close)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}