package com.talkingwhale

import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.talkingwhale.databinding.ActivityOverflowBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.vo.Post
import kotlinx.android.synthetic.main.activity_overflow.*

class OverflowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOverflowBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_overflow)
        db = AppDatabase.getAppDatabase(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView()
    }

    private fun recyclerView() {
        val posts = db.postDao().loadPosts(intent.getStringArrayExtra(POST_LIST_KEY).toList())
        posts.observe(this, Observer {
            if (it != null) {
                viewAdapter = PostAdapter(it.toTypedArray(), object : PostAdapter.PostCallback {
                    override fun onClick(post: Post) {
                        val intent = Intent(this@OverflowActivity, PostViewActivity::class.java)
                        intent.putExtra(PostViewActivity.POST_KEY, post.postId)
                        startActivity(intent)
                    }
                })
                recyclerView = my_recycler_view.apply {
                    layoutManager = GridLayoutManager(this@OverflowActivity, 3)
                    adapter = viewAdapter
                }
            }
        })
    }

    companion object {
        const val POST_LIST_KEY = "POST_LIST_KEY"
    }
}
