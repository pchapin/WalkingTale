package com.talkingwhale

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.talkingwhale.databinding.ActivityPostViewBinding
import com.talkingwhale.db.AppDatabase

import kotlinx.android.synthetic.main.activity_post_view.*

class PostViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostViewBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        db = AppDatabase.getAppDatabase(this)
        postObserver()
    }

    private fun postObserver() {
        db.postDao().load(intent.getStringExtra(POST_KEY)).observe(this, Observer {
            if (it != null) {
                binding.post = it
            }
        })
    }

    companion object {
        const val POST_KEY = "POST_KEY"
    }
}
