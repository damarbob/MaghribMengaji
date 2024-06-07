package com.simsinfotekno.maghribmengaji.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck


class VolumeAdapter(
    var dataSet: List<QuranVolume>,
) :
    RecyclerView.Adapter<VolumeAdapter.ViewHolder>() {

    companion object {
        private val TAG = this.javaClass.simpleName
    }

    /* Variables */
    private val _selectedVolume = MutableLiveData<QuranVolume>().apply { value = null }
    val selectedVolume: LiveData<QuranVolume> = _selectedVolume

    /* Use cases */
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val cardView: CardView
        val imageStatus: ImageView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemVolumeTextTitle)
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

        val id = dataSet[position].id
        val name = dataSet[position].name
        val pageIds = dataSet[position].pageIds

        Log.d(TAG, "id: $id. name: $name.")

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = String.format(
            viewHolder.textView.context.getString(R.string.quran_volume),
            name
        )

        // Set the icon based on the completion status
        val status = quranVolumeStatusCheck(dataSet[position])
        when (status) {
            QuranItemStatus.FINISHED -> {
                viewHolder.imageStatus.setImageDrawable(
                    AppCompatResources.getDrawable(
                        viewHolder.imageStatus.context,
                        R.drawable.check_circle_24px
                    )
                )
            }

            QuranItemStatus.ON_PROGRESS -> {
                viewHolder.imageStatus.setImageDrawable(
                    AppCompatResources.getDrawable(
                        viewHolder.imageStatus.context,
                        R.drawable.hourglass_empty_24px
                    )
                )
            }

            QuranItemStatus.NONE -> {
                viewHolder.imageStatus.setImageDrawable(
                    AppCompatResources.getDrawable(
                        viewHolder.imageStatus.context,
                        R.drawable.arrow_forward_ios_24px
                    )
                )
            }
        }

        // Listener
        viewHolder.cardView.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putInt("volumeId", id)
//            bundle.putIntArray("pageIds", pageIds.toIntArray())
//            if (invoker is VolumeListFragment) {
//                navController.navigate(R.id.action_volumeListFragment_to_pageListFragment, bundle)
//            } else if (invoker is HomeFragment) {
//                navController.navigate(R.id.action_homeFragment_to_pageListFragment, bundle)
//            }

            // Set selected volume
            _selectedVolume.value = dataSet[position]
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
