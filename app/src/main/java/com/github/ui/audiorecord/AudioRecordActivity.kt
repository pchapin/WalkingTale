package com.github.ui.audiorecord

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.ui.common.createFile
import com.github.ui.create.CreateFragment
import java.io.File
import java.io.IOException

/**
 * Records an audio clip
 */
class AudioRecordActivity : AppCompatActivity() {

    private var mRecordButton: RecordButton? = null
    private var mRecorder: MediaRecorder? = null

    private var mPlayButton: PlayButton? = null
    private var mPlayer: MediaPlayer? = null

    private var mFinishButton: FinishButton? = null

    private var chapterKey: String? = null
    private var expositionKey: String? = null

    lateinit var audioFile: File


    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()

    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(audioFile.absolutePath)
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun stopPlaying() {
        mPlayer!!.release()
        mPlayer = null
    }

    private fun startRecording() {
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder!!.setOutputFile(audioFile.absolutePath)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioFile = createFile(this)

        val bundle = intent.extras
        chapterKey = bundle!!.getString(CreateFragment.AUDIO_KEY_CHAPTER)
        expositionKey = bundle.getString(CreateFragment.AUDIO_KEY_EXPOSITION)
        Log.i("chapterkey", chapterKey)
        Log.i("expositionKey", expositionKey)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val ll = LinearLayout(this)
        mRecordButton = RecordButton(this)
        ll.addView(mRecordButton,
                LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f))
        mPlayButton = PlayButton(this)
        ll.addView(mPlayButton,
                LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f))

        mFinishButton = FinishButton(this)
        ll.addView(mFinishButton,
                LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f))
        setContentView(ll)
    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }

        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }

    internal inner class RecordButton(ctx: Context) : android.support.v7.widget.AppCompatButton(ctx) {
        var mStartRecording = true

        var clicker = { v: View ->
            onRecord(mStartRecording)
            if (mStartRecording) {
                text = "Stop recording"
            } else {
                text = "Start recording"
            }
            mStartRecording = !mStartRecording
        }

        init {
            text = "Start recording"
            setOnClickListener(clicker)
        }
    }

    internal inner class PlayButton(ctx: Context) : android.support.v7.widget.AppCompatButton(ctx) {
        var mStartPlaying = true

        var clicker = { v: View ->
            onPlay(mStartPlaying)
            if (mStartPlaying) {
                text = "Stop playing"
            } else {
                text = "Start playing"
            }
            mStartPlaying = !mStartPlaying
        }

        init {
            text = "Start playing"
            setOnClickListener(clicker)
        }
    }

    internal inner class FinishButton(ctx: Context) : android.support.v7.widget.AppCompatButton(ctx) {

        var clicker = { v: View ->
            val result = Intent()
            var resultData: Uri? = null
            try {
                resultData = Uri.fromFile(audioFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.i("uri", resultData!!.toString())
            result.data = resultData
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        init {
            text = "Finish"
            setOnClickListener(clicker)
        }
    }

    companion object {
        private val LOG_TAG = "AudioRecordActivity"
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}