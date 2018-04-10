package com.talkingwhale.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityPostViewBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.pojos.PostType
import kotlinx.android.synthetic.main.activity_post_view.*

class PostViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostViewBinding
    private lateinit var db: AppDatabase
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        db = AppDatabase.getAppDatabase(this)
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        postObserver()
        postAudioButton()
        videoView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post_view, menu)
        if (intent.getStringExtra(POST_GROUP_KEY) == null) {
            menu?.removeItem(R.id.action_see_group)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_see_group -> {
                val i = Intent()
                i.putExtra(POST_GROUP_GROUPID_KEY, binding.post?.groupId)
                setResult(Activity.RESULT_OK, i)
                finish()
            }
        }
        return true
    }

    private fun postObserver() {
        db.postDao().load(intent.getStringExtra(POST_KEY)).observe(this, Observer {
            if (it != null) {
                if (it.type == PostType.VIDEO) {
                    video_view.setVideoURI(Uri.parse(resources.getString(R.string.s3_hostname) + it.content))
                    prepareVideo()
                }
                binding.post = it
            }
        })
    }

    private fun postAudioButton() {
        post_audio_button.setOnClickListener({
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, theme))
            } else {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(resources.getString(R.string.s3_hostname) + binding.post!!.content)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener(MediaPlayer::start)
                mediaPlayer.setOnCompletionListener {
                    post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, theme))
                }
                post_audio_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, theme))
            }
        })
    }

    private fun prepareVideo() {
        video_view.setOnPreparedListener({
            it.isLooping = true
            video_view.start()
        })
    }

    private fun videoView() {
        video_view.setMediaController(MediaController(this))
        video_view.onFocusChangeListener = View.OnFocusChangeListener { v, _ ->
            run {
                if (v.visibility != View.VISIBLE) {
                    video_view.pause()
                }
            }
        }
    }

//    private fun deletePost() {
//        mainViewModel.setCurrentUserId(MainActivity.cognitoId)
//        mainViewModel.currentUser.observe(this, Observer {
//            var user: User? = null
//            if (it != null && it.status == Status.SUCCESS) {
//                user = it.data
//                mainViewModel.deletePost(binding.post!!).observe(this, Observer {
//                    if (it != null && it.status == Status.SUCCESS) {
//                        user!!.createdPosts.remove(binding.post!!.postId)
//                        mainViewModel.putUser(user).observe(this, Observer {
//                            if (it != null && it.status == Status.SUCCESS) {
//                                Toast.makeText(this, "Post deleted.", Toast.LENGTH_SHORT).show()
//                                setResult()
//                                finish()
//                            }
//                        })
//                    }
//                })
//            }
//        })
//    }

    companion object {
        const val POST_KEY = "POST_KEY"
        const val POST_GROUP_KEY = "POST_GROUP_KEY"
        const val POST_GROUP_GROUPID_KEY = "POST_GROUP_GROUPID_KEY"
        const val RC_GROUP_POST = 99
    }
}
