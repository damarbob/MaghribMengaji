package com.simsinfotekno.maghribmengaji.ui.volumelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentVolumeListBinding
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeGridAdapter
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck

class VolumeListFragment : Fragment() {

    lateinit var binding: FragmentVolumeListBinding

    companion object {
        val TAG = VolumeListFragment::class.java.simpleName
        fun newInstance() = VolumeListFragment()
    }

    /* View models */
    private val viewModel: VolumeListViewModel by viewModels()

    /* Views */
    private lateinit var volumeAdapter: VolumeAdapter
    private lateinit var volumeGridAdapter: VolumeGridAdapter


    /* Use cases */
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        // Read quran volume from database
//        getQuranVolume()


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        //menggnati nilai score
//        val dynamicTextView: TextView = view.findViewById(R.id.itemVolumeScore)
//        // Mengubah teks secara dinamis
//        dynamicTextView.text = "Teks Baru yang Diperbarui"
//        // Contoh lain: Mengubah teks berdasarkan kondisi tertentu
//        val someCondition = true
//        if (someCondition) {
//            dynamicTextView.text = "Teks berubah berdasarkan kondisi"
//        }

        // Restore scroll position
        savedInstanceState?.let {
            val scrollY = it.getInt("scroll_position", 0)
            binding.volumeListScroll.post {
                binding.volumeListScroll.scrollTo(0, scrollY)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("scroll_position", binding.volumeListScroll.scrollY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolumeListBinding.inflate(layoutInflater, container, false)

        /* Variables */
        val lastPageId = MainApplication.studentRepository.getStudent()?.lastPageId

        /* Views */
        if (lastPageId == null) {

            // Set collapse mode to scale
            binding.volumeListCollapsingToolbarLayout.titleCollapseMode = CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_SCALE

            // Hide last written layout
            binding.volumeListLayoutLastWritten.visibility = View.GONE
            binding.volumeListTextLastWritten.text = getString(R.string.no_data)
            binding.volumeListTextLastWrittenVolume.text = getString(R.string.no_data)

        } else {

            // Set collapse mode to fade
            binding.volumeListCollapsingToolbarLayout.titleCollapseMode = CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_FADE

            binding.volumeListTextLastWritten.text = String.format(
                requireContext().getString(R.string.page_x),
                lastPageId.toString()
            )
            binding.volumeListTextLastWrittenVolume.text = String.format(
                requireContext().getString(R.string.volume_x),
                MainApplication.quranVolumeRepository.getRecordByPageId(lastPageId)?.name
            )

        }

        // Initialize RecyclerView with default view type
        setupRecyclerView()

        /* Listeners */
        binding.volumeListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.volumeListAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val limitOffset = appBarLayout.totalScrollRange * 0.9

            if (verticalOffset == 0 || Math.abs(verticalOffset) <= limitOffset) {
                // Half expanded
                binding.volumeListCollapsingToolbarLayout.isTitleEnabled = lastPageId == null
            }
            else if (Math.abs(verticalOffset) >= limitOffset) {
                // Half collapsed
                binding.volumeListCollapsingToolbarLayout.isTitleEnabled = true
            }
        }
        binding.volumeListButtonViewTypeGrid.setOnClickListener {
            setupViewType(VolumeAdapter.VIEW_ITEM_GRID)
            it.visibility = View.GONE
            binding.volumeListButtonViewTypeList.visibility = View.VISIBLE
        }

        binding.volumeListButtonViewTypeList.setOnClickListener {
            setupViewType(VolumeAdapter.VIEW_ITEM_LIST)
            it.visibility = View.GONE
            binding.volumeListButtonViewTypeGrid.visibility = View.VISIBLE
        }

        return binding.root
    }

    // RecyclerView adapter based on view type
    private fun setupRecyclerView() {

        /* Observers */
        // Observe the selected volume LiveData
        volumeAdapter = VolumeAdapter(
            quranVolumes,
            findNavController(),
            this
        ) // Set dataset
        volumeAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null) return@observe
        }

        // Set the RecyclerView layout manager and adapter based on view type
        binding.volumeListRecyclerViewVolumeList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = volumeAdapter
        }

        // Notify adapter of data set change
        volumeAdapter.notifyDataSetChanged()

        /* Observers */
        // Observe the selected volume LiveData of grid view type
        volumeGridAdapter = VolumeGridAdapter(
            quranVolumes.reversed(),
            findNavController(),
            this,
            requireContext()
        )
        volumeGridAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null) return@observe
        }

        // Set the RecyclerView layout manager and adapter based on view type
        binding.volumeListRecyclerViewVolumeListGrid.apply {
            layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            adapter = volumeGridAdapter

        }

        // Notify adapter of data set change
        volumeGridAdapter.notifyDataSetChanged()

    }

    private fun setupViewType(viewType: Int) {
        val materialFadeThrough = MaterialFadeThrough()
        TransitionManager.beginDelayedTransition(binding.root, materialFadeThrough)
        if (viewType == VolumeAdapter.VIEW_ITEM_LIST) {
            binding.volumeListRecyclerViewVolumeListGrid.visibility = View.GONE
            binding.volumeListRecyclerViewVolumeList.visibility = View.VISIBLE
        } else {
            binding.volumeListRecyclerViewVolumeList.visibility = View.GONE
            binding.volumeListRecyclerViewVolumeListGrid.visibility = View.VISIBLE
        }
    }

    // Read quran volume from database
    private fun getQuranVolume() {

        val db = Firebase.firestore

        Log.d(TAG, "db = $db")

        db.collection("quranVolumes").get()
            .addOnSuccessListener { documents ->

                val gson = Gson()

                val quranVolumesList: List<QuranVolume> = documents.map { document ->
                    val data = document.data
                    val json = gson.toJson(data)
                    gson.fromJson(json, QuranVolume::class.java)
                }

                Log.d(TAG, "$quranVolumesList")

                volumeAdapter.dataSet = quranVolumesList
                volumeAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    override fun onResume() {
        super.onResume()

        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.mainBottomNavigationView)
        bottomNavigationView?.menu?.findItem(R.id.menu_bottom_nav_volume)?.isChecked = true
    }
}