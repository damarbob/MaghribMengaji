package com.simsinfotekno.maghribmengaji.ui.adapter

import android.graphics.drawable.Drawable
import android.os.Bundle
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
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.home.HomeFragment
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck


class VolumeGridAdapter(
    var dataSet: List<QuranVolume>,
    private val navController: NavController,
    private val invoker: Any,
) :
    RecyclerView.Adapter<VolumeGridAdapter.ViewHolder>() {

    companion object {
        private val TAG = this::class.java.simpleName
        const val VIEW_ITEM_LIST = 1
        const val VIEW_ITEM_GRID = 2
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
        /* Old */
        val textView: TextView
        val cardView: CardView
        val imageStatus: ImageView
        val imageCover: ImageView
        val imageProgress: CircularProgressIndicator

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemVolumeTwoColumnsTextTitle)
            cardView = view.findViewById(R.id.itemVolumeTwoColumnsCardView)
            imageStatus = view.findViewById(R.id.itemVolumeTwoColumnsStatus)
            imageCover = view.findViewById(R.id.itemVolumeTwoColumnsImageViewCover)
            imageProgress =
                view.findViewById(R.id.itemVolumeTwoColumnsImageProgress)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, viewType.toString())
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_volume_two_columns, viewGroup, false)

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
            viewHolder.textView.context.getString(R.string.volume_x),
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

        // Load image cover
        Glide.with(viewHolder.imageCover.context)
            .load(R.mipmap.vector_maghrib_mengaji) // TODO: change to volume cover
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.imageProgress.visibility = View.GONE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.imageProgress.visibility = View.GONE
//                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT)
//                            .show()
                    return false
                }
            })
            .into(viewHolder.imageCover)

        // Listener
        viewHolder.cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("volumeId", id)
            bundle.putIntArray("pageIds", pageIds.toIntArray())
            if (invoker is VolumeListFragment) {
                navController.navigate(R.id.action_volumeListFragment_to_pageListFragment, bundle)
            } else if (invoker is HomeFragment) {
                navController.navigate(R.id.action_homeFragment_to_pageListFragment, bundle)
            }

            // Set selected volume
            _selectedVolume.value = dataSet[position]
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}
