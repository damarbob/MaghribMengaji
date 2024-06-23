package com.simsinfotekno.maghribmengaji.ui.ustadhscoring

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.ustadhQuranPageStudentRepository
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhPageStudentBinding
import com.simsinfotekno.maghribmengaji.usecase.RetrieveQuranPageStudent

class UstadhPageStudentFragment : Fragment() {

    private lateinit var binding: FragmentUstadhPageStudentBinding

    companion object {
        private val TAG = UstadhPageStudentFragment::class.java.simpleName
        fun newInstance() = UstadhPageStudentFragment()
    }

    private val viewModel: UstadhScoringViewModel by activityViewModels()

    /* Use cases */
    private val retrieveQuranPageStudent = RetrieveQuranPageStudent()

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
        binding = FragmentUstadhPageStudentBinding.inflate(layoutInflater, container, false)

        /* Arguments */
        val pageId = arguments?.getInt("pageId")

        val page = ustadhQuranPageStudentRepository.getRecordByPageId(pageId)
        Log.d(TAG, "Page: $page")

        /* Views */

        // Image
        Glide
            .with(requireContext())
            .load(Uri.parse(page?.pictureUriString))
            .into(binding.ustadhScoringStudentImage)
            .clearOnDetach()

        // Text
        binding.ustadhScoringCollapsingToolbarLayout.title =
            getString(R.string.page_x, page?.pageId.toString())
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

        /* Observers */
        viewModel.updateStudentScoreResult.observe(viewLifecycleOwner) { result ->
            result?.onSuccess {

                Log.d(TAG, "Requesting update for student id ${page?.studentId}")
                page?.studentId?.let { id ->

                    // Refresh data to show the updated score
                    retrieveQuranPageStudent(id) {
                        ustadhQuranPageStudentRepository.setRecords(it, false)

                        // Reset live data to prevent devil loop on updateStudentScoreResult observation
                        viewModel.resetLiveData()

                        // Do not forget to repass the arguments otherwise NullPointerException
                        val bundle = Bundle()
                        if (pageId != null) {
                            bundle.putInt("pageId", pageId)
                        }
                        findNavController().navigate(R.id.action_global_ustadhPageStudentFragment, bundle)
                    }
                }

            }
        }

        /* Listeners */
        binding.ustadhScoringButton.setOnClickListener {

            val ustadhScoringDialogFragment = UstadhScoringDialogFragment()

            val bundle = Bundle()
            bundle.putString("studentId", page?.studentId)
            bundle.putInt("pageId", pageId!!)
            bundle.putInt("tidinessScore", tidinessScore)
            bundle.putInt("accuracyScore", accuracyScore)
            bundle.putInt("consistencyScore", consistencyScore)

            ustadhScoringDialogFragment.arguments = bundle
            ustadhScoringDialogFragment.show(parentFragmentManager, "scoringFragment")

        }

        // Toolbar
        binding.ustadhScoringToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_score -> {

                    val ustadhScoringDialogFragment = UstadhScoringDialogFragment()

                    val bundle = Bundle()
                    bundle.putString("studentId", page?.studentId)
                    bundle.putInt("pageId", pageId!!)
                    bundle.putInt("tidinessScore", tidinessScore)
                    bundle.putInt("accuracyScore", accuracyScore)
                    bundle.putInt("consistencyScore", consistencyScore)

                    ustadhScoringDialogFragment.arguments = bundle
                    ustadhScoringDialogFragment.show(parentFragmentManager, "scoringFragment")
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }
        binding.ustadhScoringAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                // Collapsed
                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
                    true
                )
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
                binding.ustadhScoringToolbar.menu.findItem(R.id.menu_score).setVisible(
                    false
                )
            }
        }

        return binding.root
    }
}