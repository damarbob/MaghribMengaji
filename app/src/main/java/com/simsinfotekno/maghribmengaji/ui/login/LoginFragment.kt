package com.simsinfotekno.maghribmengaji.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        /* Observers */
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            result?.onSuccess {
                // Navigate to the next screen or update UI
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.salam)}\n${getString(R.string.welcome_back)}, ${it.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()  // Finish the current activity so the user can't navigate back to the login screen

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })

        /* Listeners */
        binding.loginTextForgotPassword.setOnClickListener {

            val email = binding.loginInputEmailAddress.text.toString()

            validateEmailAndProceed(email) { validatedEmail ->
                // Show confirmation dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.auth_forgot_password))
                    .setMessage(getString(R.string.continue_please_ensure_the_data_you_entered_is_correct))
                    .setPositiveButton(getString(R.string.send)) { dialog, which ->
                        viewModel.sendPasswordResetEmail(validatedEmail) { success, errorMessage ->
                            if (success) {
                                Toast.makeText(requireContext(), getString(R.string.an_email_containing_a_passowrd_reset_link_has_been_sent), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNeutralButton(getString(R.string.close)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

        }
        binding.loginButton.setOnClickListener {

            val email = binding.loginInputEmailAddress.text.toString()
            val password = binding.loginInputPassword.text.toString()

            validateAndProceed(email, password) { _ ->
                viewModel.loginWithEmailPassword(email, password)
            }

        }
        binding.loginSignUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return binding.root
    }

    fun validateAndProceed(
        email: String,
        password: String,
        onSuccess: (String) -> Unit
    ) {
        when {
            email.isEmpty() -> {
                showErrorDialog(getString(R.string.email_is_required))
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showErrorDialog(getString(R.string.invalid_email_format))
            }

            password.isEmpty() -> {
                showErrorDialog(getString(R.string.password_is_required))
            }

            else -> {
                onSuccess(email)
            }
        }
    }

    fun validateEmailAndProceed(
        email: String,
        onSuccess: (String) -> Unit
    ) {
        when {
            email.isEmpty() -> {
                showErrorDialog(getString(R.string.email_is_required))
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showErrorDialog(getString(R.string.invalid_email_format))
            }

            else -> {
                onSuccess(email)
            }
        }
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