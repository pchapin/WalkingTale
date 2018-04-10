package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityMyPostsBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.Post
import com.talkingwhale.pojos.Status
import com.talkingwhale.ui.PostAdapter
import kotlinx.android.synthetic.main.activity_my_posts.*

class MyPostsActivity : AppCompatActivity(), DataBindingComponent {

    lateinit var binding: ActivityMyPostsBinding
    lateinit var db: AppDatabase
    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainViewModel: MainViewModel

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
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(this, object : PostAdapter.PostClickCallback {
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
    }

    private fun postListObserver() {
        mainViewModel.usersPosts.observe(this, Observer {
            if (it != null && it.status == Status.SUCCESS) {
                adapter.replace(it.data)
                adapter.notifyDataSetChanged()
            }
        })
        mainViewModel.setCurrentUserId(MainActivity.cognitoId)
    }
}
