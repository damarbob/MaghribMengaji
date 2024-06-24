package com.simsinfotekno.maghribmengaji.ui.pagelist

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.simsinfotekno.maghribmengaji.LoginActivity
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranPageRepository
import com.simsinfotekno.maghribmengaji.MainViewModel
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentPageListBinding
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranPage
import com.simsinfotekno.maghribmengaji.ui.adapter.PageAdapter
import com.simsinfotekno.maghribmengaji.ui.page.PageViewModel
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import com.simsinfotekno.maghribmengaji.usecase.GetQuranPageRangeString
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStatusCheck

class PageListFragment : Fragment() {

    companion object {
        fun newInstance() = PageListFragment()
    }

    private var _binding: FragmentPageListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PageListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val pageViewModel: PageViewModel by viewModels()

    private lateinit var pageAdapter: PageAdapter

    /* Use case */
    private val quranPageStatusCheck = QuranPageStatusCheck()
    private val getQuranPageRangeString = GetQuranPageRangeString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        // Use case initialization
//        quranPageStatusCheck = QuranPageStatusCheck()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPageListBinding.inflate(inflater, container, false)

        /* Arguments */
        val volumeId = arguments?.getInt("volumeId")
        val pageIds = arguments?.getIntArray("pageIds")!!

        /* Variables */
        val pages = quranPageRepository.getRecordByIds(pageIds)
        val pagesFinished = pages.filter { quranPageStatusCheck(it) == QuranItemStatus.FINISHED }.toMutableList().size

        /* Views */

        // Initialize data set
        pageAdapter = PageAdapter(
            pages,
            findNavController(),
            this
        )

        // Page list
        val recyclerView: RecyclerView = _binding!!.pageListRecyclerViewPage
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = pageAdapter

        // Page info
        binding.pageListTextViewVolume.text = String.format(
            binding.pageListTextViewVolume.context.getString(R.string.volume_x),
            volumeId
        )
        binding.pageListTextPageRange.text = volumeId?.let { getQuranPageRangeString(it, getString(R.string.page)) }
        binding.pageListTextPageCompleted.text = String.format(getString(R.string.x_pages_completed), pagesFinished)
//        binding.pageListTextViewPageCompletion.text =
//            "0" //TODO: add page completion logic or function
//        binding.pageListTextViewAverageScore.text = "20" //TODO: add average score logic or function

        // Listeners
        binding.pageListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.pageListAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.25

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {
                // Half expanded
                binding.pageListCollapsingToolbarLayout.isTitleEnabled = false
            }
            else if (Math.abs(verticalOffset) >= limitOffset) {
                // Half collapsed
                binding.pageListCollapsingToolbarLayout.isTitleEnabled = true
            }
        }
//        binding.pageListButtonMenu.setOnClickListener {
//            showMenu(it, R.menu.menu_main)
//        }

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
                R.id.menu_profile -> findNavController().navigate(R.id.action_pageListFragment_to_profileFragment)
                R.id.menu_sign_out -> {
                    mainViewModel.logout()
                    navigateToLoginActivity()
                }
            }
            return@setOnMenuItemClickListener false
        }
        popup.show()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Optional: Finish MainActivity to prevent the user from coming back to it
    }

    // Read quran page from firebase database
    private fun getQuranPage() {

        val db = Firebase.firestore

        Log.d(VolumeListFragment.TAG, "db = $db")

        db.collection("quranPages").get()
            .addOnSuccessListener { documents ->

                val gson = Gson()

                val quranPagesList: List<QuranPage> = documents.map { document ->
                    val data = document.data
                    val json = gson.toJson(data)
                    gson.fromJson(json, QuranPage::class.java)
                }

                Log.d(VolumeListFragment.TAG, "$quranPagesList")

                pageAdapter.dataSet = quranPagesList
                pageAdapter.notifyDataSetChanged()

                quranPageRepository.setRecords(quranPagesList, false)

            }
            .addOnFailureListener { exception ->
                Log.d(VolumeListFragment.TAG, "get failed with ", exception)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_to_fetch_data),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}