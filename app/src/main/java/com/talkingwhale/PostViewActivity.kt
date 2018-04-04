package com.talkingwhale

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import com.talkingwhale.databinding.ActivityPostViewBinding
import com.talkingwhale.db.AppDatabase
import com.talkingwhale.vo.PostType
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
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

//    private fun deletePostButton() {
//        delete_post_button.setOnClickListener({
//            mainViewModel.deletePost(binding.post!!).observe(this, Observer {
//                if (it != null && it.status == Status.SUCCESS) {
//                    currentUser.createdPosts.remove(binding.post!!.postId)
//                    mainViewModel.putUser(currentUser).observe(this, Observer {
//                        if (it != null && it.status == Status.SUCCESS) {
//                            onBackPressed()
//                            Toast.makeText(this, "Post deleted.", Toast.LENGTH_SHORT).show()
//                            mainViewModel.setPostBounds(lastCornerLatLng)
//                            mClusterManager.cluster()
//                        }
//                    })
//                }
//            })
//        })
//    }

    companion object {
        const val POST_KEY = "POST_KEY"
    }
}
