package com.simsinfotekno.maghribmengaji.ui.signup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.simsinfotekno.maghribmengaji.LoginActivity
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSignUpBinding
import com.simsinfotekno.maghribmengaji.model.CountryCode


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    companion object {
        fun newInstance() = SignUpFragment()
        private const val RC_SIGN_IN = 9001
    }

    private val viewModel: SignUpViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        binding.signUpTextInputLayoutSchool.helperText =
            "${resources.getString(R.string.npsn_stand_for)}. ${resources.getString(R.string.search_npsn_by_click_search_icon)}"
        binding.signUpTextInputLayoutSchool.setEndIconOnClickListener {
            // Open terms and conditions in browser
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.search_npsn_url))
                )
            startActivity(browserIntent)
        }

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
        viewModel.progressVisibility.observe(viewLifecycleOwner) { isVisible->
            binding.signUpLoading.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.signUpInputFullName.isEnabled = !isVisible
            binding.signUpInputEmailAddress.isEnabled = !isVisible
            binding.signUpInputPhoneNumber.isEnabled = !isVisible
            binding.signUpSpinnerCountryCode.isEnabled = !isVisible
            binding.signUpInputAddress.isEnabled = !isVisible
            binding.signUpInputSchool.isEnabled = !isVisible
            binding.signUpInputPassword.isEnabled = !isVisible
            binding.signUpInputReferral.isEnabled = !isVisible
            binding.signUpLoginGoogleButton.isEnabled = !isVisible
            binding.signUpLoginText.isEnabled = !isVisible
            binding.signUpButton.isEnabled = !isVisible
        }

        setUpCountryCodeSpinner()

        /* Listeners */
        setupListeners()
        binding.signUpButton.setOnClickListener {
            if (isValidate()) {
                val displayName = binding.signUpInputFullName.text.toString()
                val email = binding.signUpInputEmailAddress.text.toString()
                val selectedCountryCode =
                    binding.signUpSpinnerCountryCode.selectedItem as CountryCode
                var phone = binding.signUpInputPhoneNumber.text.toString()
                val password = binding.signUpInputPassword.text.toString()
                val address = binding.signUpInputAddress.text.toString()
                val school = binding.signUpInputSchool.text.toString()
                val referral = binding.signUpInputReferral.text.toString()

                // Strip leading zeros
                while (phone.startsWith("0")) {
                    phone = phone.substring(1)
                }
                phone = "${selectedCountryCode.code}$phone"

                // Show confirmation dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.auth_signup))
                    .setMessage(getString(R.string.continue_please_ensure_the_data_you_entered_is_correct))
                    .setPositiveButton(getString(R.string.send)) { dialog, which ->
                        viewModel.signUpWithEmailPassword(
                            displayName,
                            email,
                            phone,
                            password,
                            address,
                            school,
                            referral
                        )
                    }
                    .setNeutralButton(getString(R.string.close)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding.signUpLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
        binding.signUpLoginGoogleButton.setOnClickListener {
            // Do google login
            (requireActivity() as LoginActivity).signIn()
        }

        return binding.root
    }

    private fun setupListeners() {
        binding.signUpInputFullName.addTextChangedListener { validateFullName() }
        binding.signUpInputEmailAddress.addTextChangedListener { validateEmail() }
        binding.signUpInputPhoneNumber.addTextChangedListener { validatePhoneNumber() }
        binding.signUpInputAddress.addTextChangedListener { validateAddress() }
        binding.signUpInputSchool.addTextChangedListener { validateSchool() }
        binding.signUpInputPassword.addTextChangedListener { validatePassword() }
    }

    private fun isValidate(): Boolean {
        // Validate all fields simultaneously
        val isFullNameValid = validateFullName()
        val isEmailValid = validateEmail()
        val isPhoneNumberValid = validatePhoneNumber()
        val isAddressValid = validateAddress()
        val isSchoolValid = validateSchool()
        val isPasswordValid = validatePassword()

        // Return true only if all fields are valid
        return isFullNameValid && isEmailValid && isPhoneNumberValid && isAddressValid && isSchoolValid && isPasswordValid
    }

    private fun validateFullName(): Boolean {
        return if (binding.signUpInputFullName.text.isNullOrBlank()) {
            binding.signUpTextInputLayoutFullName.error =
                resources.getString(R.string.full_name_is_required)
            false
        } else {
            binding.signUpTextInputLayoutFullName.isErrorEnabled = false
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.signUpInputEmailAddress.text.toString()
        return when {
            email.isBlank() -> {
                binding.signUpTextInputLayoutEmail.error =
                    resources.getString(R.string.email_is_required)
                false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.signUpTextInputLayoutEmail.error =
                    resources.getString(R.string.invalid_email_format)
                false
            }

            else -> {
                binding.signUpTextInputLayoutEmail.isErrorEnabled = false
                true
            }
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phone = binding.signUpInputPhoneNumber.text.toString()
        return if (phone.isBlank() || !android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.signUpTextInputLayoutPhone.error = resources.getString(R.string.invalid_phone)
            false
        } else {
            binding.signUpTextInputLayoutPhone.isErrorEnabled = false
            true
        }
    }

    private fun validateAddress(): Boolean {
        return if (binding.signUpInputAddress.text.isNullOrBlank()) {
            binding.signUpTextInputLayoutAddress.error =
                resources.getString(R.string.address_is_required)
            false
        } else {
            binding.signUpTextInputLayoutAddress.isErrorEnabled = false
            true
        }
    }

    private fun validateSchool(): Boolean {
        val school = binding.signUpInputSchool.text
        return if (school!!.length != 8 && school.isNotEmpty()) {
            binding.signUpTextInputLayoutSchool.error =
                resources.getString(R.string.npsn_is_invalid)
            false
        } else {
            binding.signUpTextInputLayoutSchool.isErrorEnabled = false
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.signUpInputPassword.text.toString()
        return when {
            password.isBlank() -> {
                binding.signUpTextInputLayoutPassword.error =
                    resources.getString(R.string.password_is_required)
                false
            }

            password.length < 6 -> {
                binding.signUpTextInputLayoutPassword.error =
                    resources.getString(R.string.password_must_be_at_least_6_characters_long)
                false
            }

            else -> {
                binding.signUpTextInputLayoutPassword.isErrorEnabled = false
                true
            }
        }
    }

    private fun setUpCountryCodeSpinner() {
        val countryCodes = listOf(
            CountryCode("+62", "ID"),
            CountryCode("+1", "USA"),
            CountryCode("+44", "UK"),
            CountryCode("+91", "India"),
            // Add more country codes as needed
        )

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countryCodes)
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

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1064367673906-jkqf4cu7n6nsrr2vd2frjkjc7kj2gp6r.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(requireContext(), "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}