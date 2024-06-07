package com.simsinfotekno.maghribmengaji.ui.volumelist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.databinding.FragmentVolumeListBinding
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.adapter.VolumeAdapter
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

    /* Use cases */
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel

        // Set the transition for this fragment
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)

        // Read quran volume from database
//        getQuranVolume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolumeListBinding.inflate(layoutInflater, container, false)

        /* Views */

        // Volume dataset are taken from MainActivity local variable
        volumeAdapter = VolumeAdapter(
            quranVolumes,
        ) // Set dataset

        val recyclerView = binding.volumeListRecyclerViewVolumeList
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        /* Observers */
        volumeAdapter.selectedVolume.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            val bundle = Bundle()
            bundle.putInt("volumeId", it.id)
            bundle.putIntArray("pageIds", it.pageIds.toIntArray())
            findNavController().navigate(R.id.action_volumeListFragment_to_pageListFragment, bundle)
        }

        return binding.root
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
}