package com.talkingwhale.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityMyPostsBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.Post
import com.talkingwhale.pojos.Status
import com.talkingwhale.pojos.User
import com.talkingwhale.ui.PostAdapter
import kotlinx.android.synthetic.main.activity_my_posts.*


class MyPostsActivity : AppCompatActivity(), DataBindingComponent {

    lateinit var binding: ActivityMyPostsBinding
    lateinit var db: AppDatabase
    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainViewModel: MainViewModel
    private val displayList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_posts)
        db = AppDatabase.getAppDatabase(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupRecyclerView()
        postListObserver()
        val i = Intent()
        setResult(Activity.RESULT_OK, i)
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(this,
                object : PostAdapter.PostClickCallback {
                    override fun onClick(post: Post) {
                        db.postDao().insert(post)
                        val intent = Intent(this@MyPostsActivity, PostViewActivity::class.java)
                        intent.putExtra(PostViewActivity.POST_KEY, post.postId)
                        startActivity(intent)
                    }
                })
        recyclerView = my_post_list.apply {
            layoutManager = LinearLayoutManager(this@MyPostsActivity)
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
                        adapter.notifyDataSetChanged()

                        Snackbar.make(binding.root, "Post deleted", Snackbar.LENGTH_LONG)
                                .setAction("undo", {
                                    displayList.add(itemIndex, toBeDeleted)
                                    adapter.notifyDataSetChanged()
                                }).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
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
                            }
                        })
                    }
                })
            }
        })
    }
}
