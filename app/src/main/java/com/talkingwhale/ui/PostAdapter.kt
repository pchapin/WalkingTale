package com.talkingwhale.ui

import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.talkingwhale.R
import com.talkingwhale.databinding.ItemPostBinding
import com.talkingwhale.pojos.Post
import com.talkingwhale.util.DataBoundListAdapter
import java.util.*

class PostAdapter(
        private var dataBindingComponent: DataBindingComponent,
        private val callback: PostDeleteCallback) :
        DataBoundListAdapter<Post, ItemPostBinding>() {

    override fun createBinding(parent: ViewGroup?): ItemPostBinding {
        val binding: ItemPostBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent!!.context),
                        R.layout.item_post, parent, false,
                        dataBindingComponent)
        binding.root.setOnClickListener {
            val note = binding.post
            if (note != null) {
                callback.onClick(note)
            }
        }
        return binding
    }

    override fun bind(binding: ItemPostBinding?, item: Post?) {
        binding!!.post = item
    }

    override fun areItemsTheSame(oldItem: Post?, newItem: Post?): Boolean {
        return Objects.equals(oldItem?.postId, newItem?.postId)
    }

    override fun areContentsTheSame(oldItem: Post?, newItem: Post?): Boolean {
        return Objects.equals(oldItem?.content, newItem?.content)
    }

    interface PostDeleteCallback {
        fun onClick(post: Post)
    }
}