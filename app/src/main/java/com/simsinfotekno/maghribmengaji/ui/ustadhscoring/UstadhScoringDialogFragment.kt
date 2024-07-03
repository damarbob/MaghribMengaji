package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhScoringDialogBinding

class UstadhScoringDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentUstadhScoringDialogBinding

    private val viewModel: UstadhScoringViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUstadhScoringDialogBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val studentId = arguments?.getString("studentId")
        val pageId = arguments?.getInt("pageId")
        val initialTidinessScore = arguments?.getInt("tidinessScore")
        val initialAccuracyScore = arguments?.getInt("accuracyScore")
        val initialConsistencyScore = arguments?.getInt("consistencyScore")

        /* Views */
        binding.ustadhScoringInputTidiness.setText(initialTidinessScore.toString())
        binding.ustadhScoringInputAccuracy.setText(initialAccuracyScore.toString())
        binding.ustadhScoringInputConsistency.setText(initialConsistencyScore.toString())

        /* Observers */
        viewModel.updateStudentScoreResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_successful),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        /* Listeners */
        setupListeners()
        binding.ustadhScoringButton.setOnClickListener {

            if (isValidate()) {
                val tidinessScore = binding.ustadhScoringInputTidiness.text.toString().toInt()
                val accuracyScore = binding.ustadhScoringInputAccuracy.text.toString().toInt()
                val consistencyScore = binding.ustadhScoringInputConsistency.text.toString().toInt()

                try {
                    viewModel.updateStudentScore(
                        studentId!!,
                        pageId!!,
                        tidinessScore,
                        accuracyScore,
                        consistencyScore
                    )

                } catch (e: NumberFormatException) {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(getString(R.string.invalid_input))
                        .setMessage(getString(R.string.please_enter_valid_numbers))
                        .setPositiveButton(getString(R.string.okay)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }

//            val tidinessScore = binding.ustadhScoringInputTidiness.text.toString().toInt()
//            val accuracyScore = binding.ustadhScoringInputAccuracy.text.toString().toInt()
//            val consistencyScore = binding.ustadhScoringInputConsistency.text.toString().toInt()
//
//            try {
//
//                // Check if any score is above 100
//                if (tidinessScore > 100 || accuracyScore > 100 || consistencyScore > 100) {
//                    MaterialAlertDialogBuilder(requireActivity())
//                        .setTitle(getString(R.string.invalid_score))
//                        .setMessage(getString(R.string.score_cannot_be_more_than_100))
//                        .setPositiveButton(getString(R.string.okay)) { dialog, _ -> dialog.dismiss() }
//                        .show()
//                } else {
//                    viewModel.updateStudentScore(
//                        studentId!!,
//                        pageId!!,
//                        tidinessScore,
//                        accuracyScore,
//                        consistencyScore
//                    )
//                }
//            } catch (e: NumberFormatException) {
//                MaterialAlertDialogBuilder(requireActivity())
//                    .setTitle(getString(R.string.invalid_input))
//                    .setMessage(getString(R.string.please_enter_valid_numbers))
//                    .setPositiveButton(getString(R.string.okay)) { dialog, _ -> dialog.dismiss() }
//                    .show()
//            }

        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validateAccuracyScoring() && validateAccuracyScoring() && validateConsistencyScoring()


    private fun setupListeners() {
        binding.ustadhScoringInputTidiness.addTextChangedListener(TextFieldValidation(binding.ustadhScoringInputTidiness))
        binding.ustadhScoringInputAccuracy.addTextChangedListener(TextFieldValidation(binding.ustadhScoringInputAccuracy))
        binding.ustadhScoringInputConsistency.addTextChangedListener(TextFieldValidation(binding.ustadhScoringInputConsistency))
    }

    /**
     * applying text watcher on each text field
     */
    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // checking ids of each text field and applying functions accordingly.
            when (view.id) {
                R.id.ustadhScoringInputTidiness -> {
                    validateTidinessScoring()
                }

                R.id.ustadhScoringInputAccuracy -> {
                    validateAccuracyScoring()
                }

                R.id.ustadhScoringInputConsistency -> {
                    validateConsistencyScoring()
                }
            }
        }
    }

    /**
     * 1) field must not be empty
     * 2) score not higher than 100
     */
    private fun validateTidinessScoring(): Boolean {
        if (binding.ustadhScoringInputTidiness.text.isNullOrBlank()) {
            binding.ustadhScoringTextInputLayoutTidiness.error =
                resources.getString(R.string.must_be_filled)
            binding.ustadhScoringInputTidiness.requestFocus()
            return false
        } else if (binding.ustadhScoringInputTidiness.text.toString().toInt() > 100) {
            binding.ustadhScoringTextInputLayoutTidiness.error =
                resources.getString(R.string.score_cannot_be_more_than_100)
            binding.ustadhScoringInputTidiness.requestFocus()
            return false
        } else {
            binding.ustadhScoringTextInputLayoutTidiness.isErrorEnabled = false
        }
        return true
    }

    /**
     * 1) field must not be empty
     * 2) score not higher than 100
     */
    private fun validateAccuracyScoring(): Boolean {
        if (binding.ustadhScoringInputAccuracy.text.isNullOrBlank()) {
            binding.ustadhScoringTextInputLayoutAccuracy.error =
                resources.getString(R.string.must_be_filled)
            binding.ustadhScoringInputAccuracy.requestFocus()
            return false
        } else if (binding.ustadhScoringInputAccuracy.text.toString().toInt() > 100) {
            binding.ustadhScoringTextInputLayoutAccuracy.error =
                resources.getString(R.string.score_cannot_be_more_than_100)
            binding.ustadhScoringInputAccuracy.requestFocus()
            return false
        } else {
            binding.ustadhScoringTextInputLayoutAccuracy.isErrorEnabled = false
        }
        return true
    }

    /**
     * 1) field must not be empty
     * 2) score not higher than 100
     */
    private fun validateConsistencyScoring(): Boolean {
        if (binding.ustadhScoringInputConsistency.text.isNullOrBlank()) {
            binding.ustadhScoringTextInputLayoutConsistency.error =
                resources.getString(R.string.must_be_filled)
            binding.ustadhScoringInputConsistency.requestFocus()
            return false
        } else if (binding.ustadhScoringInputConsistency.text.toString().toInt() > 100) {
            binding.ustadhScoringTextInputLayoutConsistency.error =
                resources.getString(R.string.score_cannot_be_more_than_100)
            binding.ustadhScoringInputConsistency.requestFocus()
            return false
        } else {
            binding.ustadhScoringTextInputLayoutConsistency.isErrorEnabled = false
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}