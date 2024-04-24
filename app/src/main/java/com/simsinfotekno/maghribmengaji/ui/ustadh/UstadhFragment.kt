package com.simsinfotekno.maghribmengaji.ui.ustadh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.simsinfotekno.maghribmengaji.databinding.FragmentUstadhBinding

class UstadhFragment : Fragment() {

    private var _binding: FragmentUstadhBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ustadhViewModel =
            ViewModelProvider(this).get(UstadhViewModel::class.java)

        _binding = FragmentUstadhBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        ustadhViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}