package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityMyPostsBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.Post
import com.talkingwhale.pojos.Status
import com.talkingwhale.pojos.User
import com.talkingwhale.ui.PostAdapter
import kotlinx.android.synthetic.main.activity_my_posts.*


class MyPostsActivity : Fragment(), DataBindingComponent {

    lateinit var db: AppDatabase
    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainViewModel: MainViewModel
    private val displayList = mutableListOf<Post>()
    private lateinit var binding: ActivityMyPostsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_my_posts, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()
    }

    private fun setup() {
        db = AppDatabase.getAppDatabase(context!!)
        mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        setupRecyclerView()
        postListObserver()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(this,
                object : PostAdapter.PostClickCallback {
                    override fun onClick(post: Post) {
                        db.postDao().insert(post)
                        val intent = Intent(context, PostViewActivity::class.java)
                        intent.putExtra(PostViewActivity.POST_KEY, post.postId)
                        intent.putExtra(PostViewActivity.HIDE_USER_BTN_KEY, true)
                        startActivity(intent)
                    }
                })

        recyclerView = my_post_list.apply {
            layoutManager = LinearLayoutManager(this@MyPostsActivity.context)
            adapter = this@MyPostsActivity.adapter
        }

        val itemTouchHelper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT, ItemTouchHelper.LEFT) {
                    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
                        return false
                    }

                    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                        val itemIndex = viewHolder.adapterPosition
                        val toBeDeleted: Post = displayList[itemIndex]
                        displayList.removeAt(itemIndex)
                        adapter.notifyItemRemoved(itemIndex)

                        Snackbar.make(binding.root, "Deleting post...", Snackbar.LENGTH_LONG)
                                .setAction("undo", {
                                    displayList.add(itemIndex, toBeDeleted)
                                    adapter.notifyItemInserted(itemIndex)
                                }).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        if (event in arrayOf(DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_CONSECUTIVE, DISMISS_EVENT_MANUAL)) {
                                            deletePost(toBeDeleted)
                                        }
                                    }
                                })
                                .show()
                    }
                })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun postListObserver() {
        mainViewModel.usersPosts.observe(this, Observer {
            if (it != null && it.status == Status.SUCCESS) {
                displayList.clear()
                displayList.addAll(it.data!!)
                adapter.replace(displayList)
                adapter.notifyDataSetChanged()
            }
        })
        mainViewModel.setCurrentUserId(MainActivity.cognitoId)
    }

    private fun deletePost(post: Post) {
        val liveData = mainViewModel.currentUser
        liveData.observe(this, Observer {
            val user: User?
            if (it != null && it.status == Status.SUCCESS) {
                user = it.data
                mainViewModel.deletePost(post).observe(this, Observer {
                    if (it != null && it.status == Status.SUCCESS) {
                        user!!.createdPosts.remove(post.postId)
                        mainViewModel.putUser(user).observe(this, Observer {
                            if (it != null && it.status == Status.SUCCESS) {
                                liveData.removeObservers(this)
                                mainViewModel.setCurrentUserId(MainActivity.cognitoId)
                                Snackbar.make(binding.root, "Post deleted", Snackbar.LENGTH_SHORT).show()
                            }
                        })
                    }
                })
            }
        })
    }
}
