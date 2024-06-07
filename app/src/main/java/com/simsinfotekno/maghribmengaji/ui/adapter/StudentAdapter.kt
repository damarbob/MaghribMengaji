package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser


class StudentAdapter(
    var dataSet: List<MaghribMengajiUser>,
) :
    RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    companion object {
        private val TAG = this.javaClass.simpleName
    }

    private val _selectedStudent = MutableLiveData<MaghribMengajiUser>().apply { value = null }
    val selectedStudent: LiveData<MaghribMengajiUser> = _selectedStudent

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView
        val cardView: CardView

        init {
            // Define click listener for the ViewHolder's View
            textName = view.findViewById(R.id.itemStudentTextName)
            cardView = view.findViewById(R.id.itemStudentCardView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_student, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val id = dataSet[position].id
        val name = dataSet[position].fullName

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textName.text = name

        // Listener
        viewHolder.cardView.setOnClickListener {
            _selectedStudent.value = dataSet[position]
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
