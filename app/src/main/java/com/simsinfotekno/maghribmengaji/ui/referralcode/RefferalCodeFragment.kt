package com.simsinfotekno.maghribmengaji.ui.referralcode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.studentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentRefferalCodeBinding
import com.simsinfotekno.maghribmengaji.usecase.GenerateReferralCodeUseCase


class RefferalCodeFragment : Fragment() {

    private lateinit var binding: FragmentRefferalCodeBinding

    companion object {
        fun newInstance() = RefferalCodeFragment()
    }

    private val viewModel: RefferalCodeViewModel by viewModels()

    /* Use cases */
    private val generateReferralCodeUseCase = GenerateReferralCodeUseCase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRefferalCodeBinding.inflate(inflater, container, false)

        val user = studentRepository.getStudent()
        val referralCode = user?.referralCode

        /* Observers */
        viewModel.editProfileResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess { updatedData ->

                val newReferralCode = updatedData["referralCode"] as String

                Toast.makeText(
                    requireContext(),
                    getString(R.string.referral_code_successfully_created),
                    Toast.LENGTH_LONG
                ).show()

                binding.referralTextCode.text = newReferralCode // Set referral code to text view

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        /* Views */
        if (referralCode == null) {
            user?.id?.let { uid ->
                // Generate referral code and assign it to user
                generateReferralCodeUseCase(uid) { result ->
                    result.onSuccess { referralCode ->
                        user.referralCode = referralCode

                        viewModel.updateProfile(
                            uid,
                            hashMapOf(
                                "referralCode" to referralCode
                            )
                        )

                    }.onFailure { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        else {
            // If referral code exist
            binding.referralTextCode.text = referralCode // Set referral code to text view
        }

        /* Listeners */
        binding.referralButtonCopy.setOnClickListener {
            // Copy referral code
            val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("${getString(R.string.referral_code)} ${user?.fullName}", binding.referralTextCode.text)
            if (clipboard == null || clip == null) return@setOnClickListener
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                requireContext(),
                getString(R.string.referral_code_copied_successfully),
                Toast.LENGTH_LONG
            ).show()
        }

        return binding.root
    }
}