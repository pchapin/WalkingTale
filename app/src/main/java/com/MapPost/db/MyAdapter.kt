package com.MapPost.db

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.MapPost.R
import com.MapPost.vo.Post
import kotlinx.android.synthetic.main.my_text_view.view.*

class MyAdapter(private val myDataset: Array<Post>) :
        RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        init {
            cardView.setOnClickListener {
                Log.i(this.javaClass.simpleName, "clicked $cardView")
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_text_view, parent, false) as CardView
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.cardView.setimag = myDataset[position]
        holder.cardView.card_view_image.setImageResource(R.drawable.ic_videocam_black_24dp)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}