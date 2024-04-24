package com.simsinfotekno.maghribmengaji.ui.similarityscore

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentSimilarityScoreBinding

class SimilarityScoreFragment : Fragment() {

    companion object {
        fun newInstance() = SimilarityScoreFragment()
    }

    private val viewModel: SimilarityScoreViewModel by viewModels()

    private var _binding: FragmentSimilarityScoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSimilarityScoreBinding.inflate(inflater,container,false)

        binding.similarityScoreCircularProgressScore.progress = 100
        binding.similarityScoreTextViewScore.text = "100"
        binding.similarityScoreTextViewDetail.text = getString(R.string.your_score_is_ssr_press_upload_to_send_your_score)

        binding.similarityScoreButtonUpload.setOnClickListener {

        }

        binding.similarityScoreButtonRetry.setOnClickListener {

        }

        binding.similarityScoreButtonClose.setOnClickListener {

        }
        return inflater.inflate(R.layout.fragment_similarity_score, container, false)
    }
}