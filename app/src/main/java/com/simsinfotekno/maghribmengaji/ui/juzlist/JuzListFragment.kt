package com.simsinfotekno.maghribmengaji.ui.juzlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simsinfotekno.maghribmengaji.databinding.FragmentJuzListBinding

class JuzListFragment : Fragment() {

    private lateinit var binding: FragmentJuzListBinding

    companion object {
        fun newInstance() = JuzListFragment()
    }

    private val viewModel: JuzListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJuzListBinding.inflate(layoutInflater, container, false)



        return binding.root
    }
}