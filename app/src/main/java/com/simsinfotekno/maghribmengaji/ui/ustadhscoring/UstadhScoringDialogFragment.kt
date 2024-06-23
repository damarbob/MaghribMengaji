package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import android.os.Bundle
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
        binding.ustadhScoringButton.setOnClickListener {

            val tidinessScore = binding.ustadhScoringInputTidiness.text.toString().toInt()
            val accuracyScore = binding.ustadhScoringInputAccuracy.text.toString().toInt()
            val consistencyScore = binding.ustadhScoringInputConsistency.text.toString().toInt()

            try {

                // Check if any score is above 100
                if (tidinessScore > 100 || accuracyScore > 100 || consistencyScore > 100) {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(getString(R.string.invalid_score))
                        .setMessage(getString(R.string.score_cannot_be_more_than_100))
                        .setPositiveButton(getString(R.string.okay)) { dialog, _ -> dialog.dismiss() }
                        .show()
                } else {
                    viewModel.updateStudentScore(
                        studentId!!,
                        pageId!!,
                        tidinessScore,
                        accuracyScore,
                        consistencyScore
                    )
                }
            } catch (e: NumberFormatException) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.invalid_input))
                    .setMessage(getString(R.string.please_enter_valid_numbers))
                    .setPositiveButton(getString(R.string.okay)) { dialog, _ -> dialog.dismiss() }
                    .show()
            }

        }

        return binding.root
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