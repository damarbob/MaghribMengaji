package com.simsinfotekno.maghribmengaji.ui.pagelist

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.InsetDrawable
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageListBinding
import com.simsinfotekno.maghribmengaji.model.QuranPage

class PageListFragment : Fragment() {

    companion object {
        fun newInstance() = PageListFragment()
    }
    private var _binding: FragmentPageListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PageListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPageListBinding.inflate(inflater,container,false)

        val dataset = arrayOf(QuranPage(1,"1"), QuranPage(2,"2"), QuranPage(3,"3"))
        val pageAdapter = PageAdapter(dataset)

        // Page list
        val recyclerView: RecyclerView = _binding!!.pageListRecyclerViewPage
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = pageAdapter

        binding.pageListTextViewPageCompletion.text = "0" //TODO: add page completion logic or function
        binding.pageListTextViewAverageScore.text = "20" //TODO: add average score logic or function

        // Listener
        binding.pageListButtonMenu.setOnClickListener {
            showMenu(it, R.menu.menu_main)
        }

        return binding.root
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