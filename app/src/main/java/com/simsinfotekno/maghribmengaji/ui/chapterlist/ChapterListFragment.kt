package com.simsinfotekno.maghribmengaji.ui.chapterlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.databinding.FragmentChapterListBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.ChapterAdapter

class ChapterListFragment : Fragment() {

    private lateinit var binding: FragmentChapterListBinding

    companion object {
        fun newInstance() = ChapterListFragment()
    }

    private val viewModel: ChapterListViewModel by viewModels()

    /* Views */

    // Adapters
    private lateinit var chapterAdapter: ChapterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChapterListBinding.inflate(layoutInflater, container, false)

        /* Views */

        // Chapter adapter
        chapterAdapter = ChapterAdapter(
            MainApplication.quranChapterRepository.getRecords(),
            findNavController(),
        )

        // Chapter list
        val chapterRecyclerView: RecyclerView = binding.chapterListRecyclerView
        chapterRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        chapterRecyclerView.adapter = chapterAdapter

        /* Listeners */
        binding.chapterListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }
}