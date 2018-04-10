package com.talkingwhale.activities

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityOverflowBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.Post
import com.talkingwhale.ui.PostAdapter
import kotlinx.android.synthetic.main.activity_overflow.*

class OverflowActivity : AppCompatActivity(), DataBindingComponent {
    private lateinit var binding: ActivityOverflowBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var db: AppDatabase
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_overflow)
        db = AppDatabase.getAppDatabase(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView()
    }

    private fun recyclerView() {
        adapter = PostAdapter(this, object : PostAdapter.PostDeleteCallback {
            override fun onClick(post: Post) {
                val intent = Intent(this@OverflowActivity, PostViewActivity::class.java)
                intent.putExtra(PostViewActivity.POST_KEY, post.postId)
                intent.putExtra(PostViewActivity.POST_GROUP_KEY, post.groupId)
                startActivityForResult(intent, PostViewActivity.RC_GROUP_POST)
            }
        })
        recyclerView = my_recycler_view.apply {
            layoutManager = GridLayoutManager(this@OverflowActivity, 2)
            adapter = this@OverflowActivity.adapter
        }

        val posts = db.postDao().loadPosts(intent.getStringArrayExtra(POST_LIST_KEY).toList())
        adapter.replace(posts)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PostViewActivity.RC_GROUP_POST) {
            val i = Intent()
            i.putExtra(PostViewActivity.POST_GROUP_GROUPID_KEY, data?.getStringExtra(PostViewActivity.POST_GROUP_GROUPID_KEY))
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }

    companion object {
        const val POST_LIST_KEY = "POST_LIST_KEY"
    }
}
