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
import com.google.gson.Gson
import com.simsinfotekno.maghribmengaji.MainActivity
import com.simsinfotekno.maghribmengaji.databinding.FragmentVolumeListBinding
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.home.VolumeAdapter

class VolumeListFragment : Fragment() {

    lateinit var binding: FragmentVolumeListBinding

    companion object {
        val TAG = VolumeListFragment::class.java.simpleName
        fun newInstance() = VolumeListFragment()
    }

    private val viewModel: VolumeListViewModel by viewModels()

    // Variables
    private lateinit var volumeAdapter: VolumeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel

        // Read quran volume from database
        getQuranVolume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolumeListBinding.inflate(layoutInflater, container, false)

        volumeAdapter = VolumeAdapter(MainActivity.quranVolumes, findNavController(),this) // Set dataset

        val recyclerView = binding.volumeListRecyclerViewVolumeList
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = volumeAdapter

        return binding.root
    }

    // Read quran volume from database
    private fun getQuranVolume() {

        val db = viewModel.db

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