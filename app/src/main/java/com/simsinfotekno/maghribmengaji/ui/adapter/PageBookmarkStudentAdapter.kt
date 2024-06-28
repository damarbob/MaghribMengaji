package com.simsinfotekno.maghribmengaji.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.model.QuranPageBookmarkStudent
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStatusCheck

class PageBookmarkStudentAdapter(
    var dataSet: List<QuranPageBookmarkStudent>,
    private val navController: NavController,
) :
    RecyclerView.Adapter<PageBookmarkStudentAdapter.ViewHolder>() {

    /* Use cases */
    private val quranPageStatusCheck = QuranPageStatusCheck()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val cardViewPage: CardView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemBookmarkName)
            cardViewPage = view.findViewById(R.id.itemBookmarkCardView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_bookmark, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val bookmark = dataSet[position]

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = String.format(
            viewHolder.textView.context.getString(R.string.juz_x),
            bookmark.name
        )

        // Listener
        viewHolder.cardViewPage.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("pageId", bookmark.pageId!!)
            navController.navigate(R.id.action_global_pageFragment, bundle)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
