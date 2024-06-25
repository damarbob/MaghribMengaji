package com.simsinfotekno.maghribmengaji.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSignUpBinding


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

        /* Listeners */
        binding.signUpButton.setOnClickListener {
            val displayName = binding.signUpInputFullName.text.toString()
            val email = binding.signUpInputEmailAddress.text.toString()
            val phone = binding.signUpInputPhoneNumber.text.toString()
            val password = binding.signUpInputPassword.text.toString()

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