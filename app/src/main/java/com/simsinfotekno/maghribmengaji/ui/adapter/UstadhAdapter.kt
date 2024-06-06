package com.simsinfotekno.maghribmengaji.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.simsinfotekno.maghribmengaji.R
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser


class UstadhAdapter(
    var dataSet: List<MaghribMengajiUser>,
    private val navController: NavController,
) :
    RecyclerView.Adapter<UstadhAdapter.ViewHolder>() {

    companion object {
        private val TAG = this.javaClass.simpleName
    }

    private val _selectedUstadh = MutableLiveData<MaghribMengajiUser>().apply { value = null }
    val selectedUstadh: LiveData<MaghribMengajiUser> = _selectedUstadh

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView
        val cardView: CardView

        init {
            // Define click listener for the ViewHolder's View
            textName = view.findViewById(R.id.itemUstadhTextName)
            cardView = view.findViewById(R.id.itemUstadhCardView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_ustadh, viewGroup, false)

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
//            val bundle = Bundle()
//            bundle.putString("ustadhUserId", id)
//            navController.navigate(navController.graph.startDestinationId, bundle)
            _selectedUstadh.value = dataSet[position]
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
