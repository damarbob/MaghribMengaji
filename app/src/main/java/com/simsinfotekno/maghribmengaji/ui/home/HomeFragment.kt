package com.simsinfotekno.maghribmengaji.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentHomeBinding
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Volume adapter
        val volumeAdapter = VolumeAdapter(
            // TODO: Handle if onProgressPageIds is null
            MainActivity.quranVolumeRepository.getRecordsById(MainActivity.testStudent.onProgressPageIds!!.toIntArray()),
            findNavController(),
            this
        ) // Set dataset

        // Volume list
        val recyclerView: RecyclerView = binding.homeRecyclerViewVolume
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        binding.homeTextLastWritten.text = String.format(requireContext().getString(R.string.quran_page), MainActivity.testStudent.lastPageId) // Last written

        // Progress indicator
        val allPagesCount = 604
        val progressPercentage = MainActivity.testStudent.finishedPageIds?.count()?.times(100)?.div(allPagesCount)
        binding.homeTextPagePercentage.text = (progressPercentage).toString()
        if (progressPercentage != null) {
            binding.homeProgressIndicatorPagePercentage.progress = if (progressPercentage < 5) 5 else progressPercentage
        }

        // Listener
        binding.homeButtonMenu.setOnClickListener {
            showMenu(it, R.menu.menu_main)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //In the showMenu function from the previous example:
    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val ICON_MARGIN = 8
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN.toFloat(), resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popup.setOnMenuItemClickListener { it ->
            when (it.itemId) {
                R.id.menu_profile -> findNavController().navigate(R.id.action_navigation_home_to_navigation_profile)
            }
            return@setOnMenuItemClickListener false
        }
        popup.show()
    }
}