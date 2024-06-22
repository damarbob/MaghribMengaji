package com.simsinfotekno.maghribmengaji.ui.adapter

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
import com.simsinfotekno.maghribmengaji.model.QuranPageStudent
import com.simsinfotekno.maghribmengaji.usecase.QuranPageStudentStatusCheck

class PageStudentAdapter(
    var dataSet: List<QuranPageStudent>,
) :
    RecyclerView.Adapter<PageStudentAdapter.ViewHolder>() {

    /* Variables */
    private val _selectedPageStudent = MutableLiveData<QuranPageStudent>().apply { value = null }
    val selectedPageStudent: LiveData<QuranPageStudent> = _selectedPageStudent

    /* Use cases */
    private val quranPageStudentStatusCheck = QuranPageStudentStatusCheck()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val cardViewPage: CardView
        val imageStatus: ImageView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.itemPageTextTitle)
            cardViewPage = view.findViewById(R.id.itemPageCardView)
            imageStatus = view.findViewById(R.id.itemPageImageStatus)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_page, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val page = dataSet[position]

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = String.format(
            viewHolder.textView.context.getString(R.string.page_x),
            page.pageId
        )

        // Set the icon based on the completion status
        val status = quranPageStudentStatusCheck(page)
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
        viewHolder.cardViewPage.setOnClickListener {
            _selectedPageStudent.value = dataSet[position]
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
