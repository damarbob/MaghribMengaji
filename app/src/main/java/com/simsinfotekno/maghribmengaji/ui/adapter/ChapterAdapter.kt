package com.simsinfotekno.maghribmengaji.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.model.QuranChapter
import com.simsinfotekno.maghribmengaji.usecase.GetChapterNameFromStringResourceUseCase

class ChapterAdapter(
    var dataSet: List<QuranChapter>,
    private val navController: NavController,
) :
    RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    /* Use case */
    private val getChapterNameFromStringResourceUseCase = GetChapterNameFromStringResourceUseCase()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val textViewVolume: TextView
        val cardView: CardView
        val imageStatus: ImageView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemVolumeTextTitle)
            textViewVolume = view.findViewById(R.id.itemVolumeTextVolume)
            cardView = view.findViewById(R.id.itemVolumeCardView)
            imageStatus = view.findViewById(R.id.itemVolumeStatus)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_volume, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val chapter = dataSet[position]

        viewHolder.imageStatus.visibility = View.GONE // Disable status

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = getChapterNameFromStringResourceUseCase(chapter.id, navController.context)
        viewHolder.textViewVolume.text = chapter.id.toString()

        // Listener
        viewHolder.cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putIntArray("pageIds", chapter.pageIds.toIntArray())
            bundle.putString("chapterName", chapter.name)
            navController.navigate(R.id.action_global_pageListFragment, bundle)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
