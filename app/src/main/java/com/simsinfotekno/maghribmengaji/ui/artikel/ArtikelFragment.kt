package com.simsinfotekno.maghribmengaji.ui.artikel

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R

class ArtikelFragment : Fragment() {

    companion object {
        fun newInstance() = ArtikelFragment()
    }

    private val viewModel: ArtikelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val visibleItemCount = layoutManager.childCount
//                val totalItemCount = layoutManager.itemCount
//                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
//
//                if (dy > 0) { // Pastikan pengguna menggulir ke bawah
//                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
//                        // Sudah mencapai bagian bawah dari RecyclerView
//                        loadMoreData()
//                    }
//                }
//            }
//        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_artikel, container, false)

    }

}