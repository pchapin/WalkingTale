package com.talkingwhale.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.talkingwhale.R
import com.talkingwhale.util.createFile
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.File
import java.io.IOException

/**
 * Records an audio clip
 */
class AudioRecordActivity : AppCompatActivity() {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private lateinit var audioFile: File
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mStartRecording = true
    private var mStartPlaying = true

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
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
        setContentView(R.layout.activity_audio_record)
        audioFile = createFile(this)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recordButton()
        playButton()
        finishButton()
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

    private fun recordButton() {
        button_audio_record.setOnClickListener {
            onRecord(mStartRecording)
            if (mStartRecording) {
                button_audio_record.text = getString(R.string.stop_recording)
            } else {
                button_audio_record.text = getString(R.string.start_recording)
            }
            mStartRecording = !mStartRecording
        }
    }

    private fun playButton() {
        button_audio_play.setOnClickListener {
            onPlay(mStartPlaying)
            if (mStartPlaying) {
                button_audio_play.text = getString(R.string.stop_playing)
            } else {
                button_audio_play.text = getString(R.string.start_playing)
            }
            mStartPlaying = !mStartPlaying
        }
    }

    private fun finishButton() {
        button_audio_finish.setOnClickListener {
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
    }

    companion object {
        private val LOG_TAG = "AudioRecordActivity"
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}