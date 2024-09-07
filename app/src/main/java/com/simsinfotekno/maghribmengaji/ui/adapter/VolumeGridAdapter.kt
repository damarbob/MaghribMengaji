package com.simsinfotekno.maghribmengaji.ui.adapter

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import com.simsinfotekno.maghribmengaji.MainApplication
import com.simsinfotekno.maghribmengaji.MainApplication.Companion.quranVolumes
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.enums.QuranItemStatus
import com.simsinfotekno.maghribmengaji.model.QuranVolume
import com.simsinfotekno.maghribmengaji.ui.home.HomeFragment
import com.simsinfotekno.maghribmengaji.ui.volumelist.VolumeListFragment
import com.simsinfotekno.maghribmengaji.usecase.GetColorFromAttrUseCase
import com.simsinfotekno.maghribmengaji.usecase.GetQuranVolumeStudentScore
import com.simsinfotekno.maghribmengaji.usecase.QuranVolumeStatusCheck


class VolumeGridAdapter(
    var dataSet: List<QuranVolume>,
    private val navController: NavController,
    private val invoker: Any,
    private val context: Context
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
    private var materialAlertDialog: AlertDialog? = null

    /* Use cases */
    private val quranVolumeStatusCheck = QuranVolumeStatusCheck()
    private val getColorFromAttrUseCase = GetColorFromAttrUseCase()
    private val quranVolumeStudentScore = GetQuranVolumeStudentScore()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /* Old */
        val textView: TextView
        val textViewVolume: TextView
        val cardView: CardView
        val imageStatus: ImageView
        val imageCover: ImageView
        val imageProgress: CircularProgressIndicator
        val textViewVolumeScore: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemVolumeTwoColumnsTextTitle)
            textViewVolume = view.findViewById(R.id.itemVolumeTextVolume)
            cardView = view.findViewById(R.id.itemVolumeTwoColumnsCardView)
            imageStatus = view.findViewById(R.id.itemVolumeTwoColumnsStatus)
            imageCover = view.findViewById(R.id.itemVolumeTwoColumnsImageViewCover)
            imageProgress =
                view.findViewById(R.id.itemVolumeTwoColumnsImageProgress)
            textViewVolumeScore = view.findViewById(R.id.itemVolumeScore)
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

        val ownedQuranVolumeId = MainApplication.studentRepository.getStudent()?.ownedVolumeId

        Log.d(TAG, "id: $id. name: $name.")

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = String.format(
            viewHolder.textView.context.getString(R.string.volume_x),
            name
        )
        viewHolder.textViewVolume.text = name
        quranVolumeStudentScore(Firebase.auth.currentUser?.uid!!, id) {
            viewHolder.textViewVolumeScore.text = it.toString()
        }

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
            .load(dataSet[position].picture)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.imageProgress.visibility = View.GONE

//                    viewHolder.imageCover.setBackgroundColor(
//                        getColorFromAttrUseCase(
//                            com.google.android.material.R.attr.colorPrimary,
//                            navController.context
//                        )
//                    )
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

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        colorMatrix.setSaturation(1f)
        val noFilter = ColorMatrixColorFilter(colorMatrix)
        if (ownedQuranVolumeId != null) {
            viewHolder.imageCover.colorFilter =
                if (!ownedQuranVolumeId.toIntArray().contains(id)) filter else noFilter
        } else viewHolder.imageCover.colorFilter = filter

        /* Listener */
        // Check owned Quran Volume
        viewHolder.cardView.setOnClickListener {
            Log.d("asdfasdfasdf", "id: ${quranVolumes.reversed()[position].id}")
            Log.d("asdfasdfasdf", "owned: $ownedQuranVolumeId")
            Log.d("asdfasdfasdf", "aslinya punya? ${ownedQuranVolumeId?.toIntArray()?.contains(quranVolumes.reversed()[position].id)}")

            if (ownedQuranVolumeId.isNullOrEmpty()) {
                showAlertDialog()
                Log.d("asdfasdfasdf", "null")
            } else {
                if (ownedQuranVolumeId.toIntArray().contains(id)) {
                    Log.d("asdfasdfasdf", "punya")
                    // If own, go to page list
                    val bundle = Bundle().apply {
                        putInt("volumeId", id)
                        putIntArray("pageIds", pageIds.toIntArray())
                    }

                    when (invoker) {
                        is VolumeListFragment -> navController.navigate(
                            R.id.action_volumeListFragment_to_pageListFragment, bundle
                        )

                        is HomeFragment -> navController.navigate(
                            R.id.action_homeFragment_to_pageListFragment, bundle
                        )
                    }

                    // Set selected volume
                    _selectedVolume.value = dataSet[position]
                } else {
                    showAlertDialog()
                    Log.d("asdfasdfasdf", "tidak")
                }
            }
        }

    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    // Function to show the Material Alert Dialog
    private fun showAlertDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.afwan))
            .setMessage(context.getString(R.string.this_volume_is_locked))
            .setPositiveButton(context.getString(R.string.okay)) { dialog, _ ->
                dialog.dismiss()  // Dismiss the dialog when OK is clicked
            }
            .show()
    }
}
