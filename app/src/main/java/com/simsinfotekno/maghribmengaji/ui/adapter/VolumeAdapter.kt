package com.simsinfotekno.maghribmengaji.ui.adapter

import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck


class VolumeAdapter(
    var dataSet: List<QuranVolume>,
    private var viewType: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
    private inner class ViewHolderViewItemList(view: View) : RecyclerView.ViewHolder(view) {
        /* Old */
//        val textView: TextView
//        val cardView: CardView
//        val imageStatus: ImageView
//
//        init {
//            // Define click listener for the ViewHolder's View
//            textView = view.findViewById(R.id.itemVolumeTextTitle)
//            cardView = view.findViewById(R.id.itemVolumeCardView)
//            imageStatus = view.findViewById(R.id.itemVolumeStatus)
//        }
        /* End of Old */

        val textView: TextView = view.findViewById(R.id.itemVolumeTextTitle)
        val textVolume: TextView = view.findViewById(R.id.itemVolumeTextVolume)
        val cardView: CardView = view.findViewById(R.id.itemVolumeCardView)
        val imageStatus: ImageView = view.findViewById(R.id.itemVolumeStatus)

        fun bind(position: Int) {
            val id = dataSet[position].id
            val name = dataSet[position].name

            Log.d(TAG, "id: $id. name: $name.")

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            textView.text = String.format(
                textView.context.getString(R.string.volume_x),
                name
            )

            textVolume.text = name

            // Set the icon based on the completion status
            val status = quranVolumeStatusCheck(dataSet[position])
            when (status) {
                QuranItemStatus.FINISHED -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.check_circle_24px
                        )
                    )
                }

                QuranItemStatus.ON_PROGRESS -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.hourglass_empty_24px
                        )
                    )
                }

                QuranItemStatus.NONE -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.arrow_forward_ios_24px
                        )
                    )
                }
            }

            // Listener
            cardView.setOnClickListener {
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
                Log.d(VolumeListFragment.TAG, _selectedVolume.toString())
            }
        }
    }

    // View holder item grid type
    private inner class ViewHolderViewItemGrid(view: View) : RecyclerView.ViewHolder(view) {
        /* Old */
//        val textView: TextView
//        val cardView: CardView
//        val imageStatus: ImageView
//        val imageCover: ImageView
//
//        init {
//            // Define click listener for the ViewHolder's View
//            textView = view.findViewById(R.id.itemVolumeTwoColumnsTextTitle)
//            cardView = view.findViewById(R.id.itemVolumeTwoColumnsCardView)
//            imageStatus = view.findViewById(R.id.itemVolumeTwoColumnsStatus)
//            imageCover = view.findViewById(R.id.itemVolumeTwoColumnsImageViewCover)
//        }
        /* End of Old */

        val textView: TextView = view.findViewById(R.id.itemVolumeTwoColumnsTextTitle)
        val textVolume: TextView = view.findViewById(R.id.itemVolumeTextVolume)
        val cardView: CardView = view.findViewById(R.id.itemVolumeTwoColumnsCardView)
        val imageStatus: ImageView = view.findViewById(R.id.itemVolumeTwoColumnsStatus)
        val imageCover: ImageView = view.findViewById(R.id.itemVolumeTwoColumnsImageViewCover)
        val imageProgress: CircularProgressIndicator =
            view.findViewById(R.id.itemVolumeTwoColumnsImageProgress)

        fun bind(position: Int) {
            val id = dataSet[position].id
            val name = dataSet[position].name

            Log.d(TAG, "id: $id. name: $name.")

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            textView.text = String.format(
                textView.context.getString(R.string.volume_x),
                name
            )

            textVolume.text = name

            // Load image cover
            Glide.with(imageCover.context)
                .load(R.drawable.ic_maghrib_mengaji_notext) // TODO: change to volume cover
                .override(64)//Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Drawable> {

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageProgress.visibility = View.GONE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageProgress.visibility = View.GONE
//                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT)
//                            .show()
                        return false
                    }
                })
                .into(imageCover)

            // Set the icon based on the completion status
            val status = quranVolumeStatusCheck(dataSet[position])
            when (status) {
                QuranItemStatus.FINISHED -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.check_circle_24px
                        )
                    )
                }

                QuranItemStatus.ON_PROGRESS -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.hourglass_empty_24px
                        )
                    )
                }

                QuranItemStatus.NONE -> {
                    imageStatus.setImageDrawable(
                        AppCompatResources.getDrawable(
                            imageStatus.context,
                            R.drawable.arrow_forward_ios_24px
                        )
                    )
                }
            }

            // Listener
            cardView.setOnClickListener {
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
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, viewType.toString())
        // Create a new view, which defines the UI of the list item
        return if (this.viewType == VIEW_ITEM_LIST) ViewHolderViewItemList(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_volume, viewGroup, false)
        ) else ViewHolderViewItemGrid(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_volume_two_columns, viewGroup, false)
        )

        /* Old */
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.item_volume, viewGroup, false)
//
//        return ViewHolder(view)
        /* End of Old */
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewType == VIEW_ITEM_LIST) (viewHolder as ViewHolderViewItemList).bind(position)
        else (viewHolder as ViewHolderViewItemGrid).bind(position)


        /* Old */
//        val id = dataSet[position].id
//        val name = dataSet[position].name
//        val pageIds = dataSet[position].pageIds
//
//        Log.d(TAG, "id: $id. name: $name.")
//
//        // Get element from your dataset at this position and replace the
//        // contents of the view with that element
//        viewHolder.textView.text = String.format(
//            viewHolder.textView.context.getString(R.string.quran_volume),
//            name
//        )
//
//        // Set the icon based on the completion status
//        val status = quranVolumeStatusCheck(dataSet[position])
//        when (status) {
//            QuranItemStatus.FINISHED -> {
//                viewHolder.imageStatus.setImageDrawable(
//                    AppCompatResources.getDrawable(
//                        viewHolder.imageStatus.context,
//                        R.drawable.check_circle_24px
//                    )
//                )
//            }
//
//            QuranItemStatus.ON_PROGRESS -> {
//                viewHolder.imageStatus.setImageDrawable(
//                    AppCompatResources.getDrawable(
//                        viewHolder.imageStatus.context,
//                        R.drawable.hourglass_empty_24px
//                    )
//                )
//            }
//
//            QuranItemStatus.NONE -> {
//                viewHolder.imageStatus.setImageDrawable(
//                    AppCompatResources.getDrawable(
//                        viewHolder.imageStatus.context,
//                        R.drawable.arrow_forward_ios_24px
//                    )
//                )
//            }
//        }
//
//        // Listener
//        viewHolder.cardView.setOnClickListener {
////            val bundle = Bundle()
////            bundle.putInt("volumeId", id)
////            bundle.putIntArray("pageIds", pageIds.toIntArray())
////            if (invoker is VolumeListFragment) {
////                navController.navigate(R.id.action_volumeListFragment_to_pageListFragment, bundle)
////            } else if (invoker is HomeFragment) {
////                navController.navigate(R.id.action_homeFragment_to_pageListFragment, bundle)
////            }
//
//            // Set selected volume
//            _selectedVolume.value = dataSet[position]
//        }
        /* End of Old */
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}
