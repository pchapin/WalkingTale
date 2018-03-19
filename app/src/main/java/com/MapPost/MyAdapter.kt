package com.MapPost

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.MapPost.vo.Post
import com.MapPost.vo.PostType.*
import kotlinx.android.synthetic.main.my_text_view.view.*

class MyAdapter(private val myDataset: Array<Post>, val callback: PostCallback) :
        RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val cardView: CardView, callback: PostCallback, myDataset: Array<Post>) : RecyclerView.ViewHolder(cardView) {
        init {
            cardView.setOnClickListener {
                Log.i(this.javaClass.simpleName, "clicked $layoutPosition $cardView")
                callback.onClick(myDataset[layoutPosition])
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_text_view, parent, false) as CardView
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view, callback, myDataset)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val post: Post = myDataset[position]
        when (post.type) {
            TEXT -> {
                holder.cardView.card_view_image.setImageResource(R.drawable.ic_textsms_black_24dp)
            }
            AUDIO -> {
                holder.cardView.card_view_image.setImageResource(R.drawable.ic_audiotrack_black_24dp)
            }
            PICTURE -> {
                holder.cardView.card_view_image.setImageResource(R.drawable.ic_camera_alt_black_24dp)
            }
            VIDEO -> {
                holder.cardView.card_view_image.setImageResource(R.drawable.ic_videocam_black_24dp)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size


    interface PostCallback {
        fun onClick(post: Post)
    }
}