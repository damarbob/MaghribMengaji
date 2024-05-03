package com.simsinfotekno.maghribmengaji.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import java.util.Objects


class VolumeAdapter(var dataSet: List<QuranVolume>, private val navController: NavController, private val invoker: Any) :
    RecyclerView.Adapter<VolumeAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val cardView: CardView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemVolumeTextTitle)
            cardView = view.findViewById(R.id.itemVolumeCardView)
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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = String.format(
            viewHolder.textView.context.getString(R.string.quran_volume),
            dataSet[position].name
        )

        // Listener
        viewHolder.cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("volumeId", dataSet[position].id)
            if (invoker is VolumeListFragment) {
                navController.navigate(R.id.action_volumeListFragment_to_pageListFragment, bundle)
            } else {
                navController.navigate(R.id.action_homeFragment_to_pageListFragment, bundle)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
