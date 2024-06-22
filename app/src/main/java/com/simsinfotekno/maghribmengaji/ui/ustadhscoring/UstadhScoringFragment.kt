package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhScoringBinding

class UstadhScoringFragment : Fragment() {

    private lateinit var binding: FragmentUstadhScoringBinding

    companion object {
        private val TAG = UstadhScoringFragment::class.java.simpleName
        fun newInstance() = UstadhScoringFragment()
    }

    private val viewModel: UstadhScoringViewModel by viewModels()

    /* Variables */
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

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
        binding = FragmentUstadhScoringBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val pageId = arguments?.getInt("pageId")

        val page = ustadhQuranPageStudentRepository.getRecordByPageId(pageId)
        Log.d(TAG, "Page: $page")

        /* Views */
        bottomSheetBehavior = BottomSheetBehavior.from(binding.ustadhScoringBottomSheet.bottomSheetUstadhScoring)

        Glide
            .with(requireContext())
            .load(Uri.parse(page?.pictureUriString))
            .into(binding.ustadhScoringStudentImage)
            .clearOnDetach()

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // Hide bottom sheet

        // Text
        binding.ustadhScoringCollapsingToolbarLayout.title = getString(R.string.page_x, page?.pageId.toString())
//        binding.ustadhScoringStudentName.text = "" // TODO: Set to student name

        // Scores
        val ocrScore = page?.oCRScore ?: 0
        val tidinessScore = page?.tidinessScore ?: 0
        val accuracyScore = page?.accuracyScore ?: 0
        val consistencyScore = page?.consistencyScore ?: 0

        val overallScore = (ocrScore + tidinessScore + accuracyScore + consistencyScore) / 4

        binding.ustadhScoringTextOverallScore.text = overallScore.toString()
        binding.ustadhScoringTextPreliminaryResult.text = ocrScore.toString()
        binding.ustadhScoringTextTidinessResult.text = tidinessScore.toString()
        binding.ustadhScoringTextAccuracyResult.text = accuracyScore.toString()
        binding.ustadhScoringTextConsistencyResult.text = consistencyScore.toString()

        binding.ustadhScoringProgressOverallScore.progress = overallScore
        binding.ustadhScoringProgressPreliminary.progress = ocrScore
        binding.ustadhScoringProgressTidiness.progress = tidinessScore
        binding.ustadhScoringProgressAccuracy.progress = accuracyScore
        binding.ustadhScoringProgressConsistency.progress = consistencyScore

        // Inputs
        binding.ustadhScoringBottomSheet.ustadhScoringInputTidiness.setText(tidinessScore.toString())
        binding.ustadhScoringBottomSheet.ustadhScoringInputAccuracy.setText(accuracyScore.toString())
        binding.ustadhScoringBottomSheet.ustadhScoringInputConsistency.setText(consistencyScore.toString())

        /* Listeners */
        binding.ustadhScoringButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        binding.ustadhScoringBottomSheet.ustadhScoringButton.setOnClickListener {

            if (page?.studentId == null) return@setOnClickListener

            val tidiness = binding.ustadhScoringBottomSheet.ustadhScoringInputTidiness.text.toString()
            val accuracy = binding.ustadhScoringBottomSheet.ustadhScoringInputAccuracy.text.toString()
            val consistency = binding.ustadhScoringBottomSheet.ustadhScoringInputConsistency.text.toString()

            viewModel.updateStudentScore(page.studentId, pageId!!, tidiness.toInt(), accuracy.toInt(), consistency.toInt())
        }

        // Toolbar
        binding.ustadhScoringToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_score -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        binding.ustadhScoringAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                // Collapsed
                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
                    true)
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
                    false )
            }
        }

        /* Observers */
        viewModel.updateStudentScoreResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                Toast.makeText(requireContext(), getString(R.string.upload_successful), Toast.LENGTH_SHORT).show()

            }?.onFailure { exception ->
                // Show error message
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        /* Events */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        return binding.root
    }
}