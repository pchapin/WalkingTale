package com.talkingwhale.activities

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityAudioRecordBinding
import com.talkingwhale.util.createFile
import com.talkingwhale.util.popBackStack
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.File
import java.io.IOException

/**
 * Records an audio clip
 */
class AudioRecordActivity : Fragment() {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private lateinit var audioFile: File
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mStartRecording = true
    private var mStartPlaying = true
    private lateinit var binding: ActivityAudioRecordBinding
    private lateinit var mainViewModel: MainViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_audio_record, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        audioFile = createFile(activity!!)
        mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        ActivityCompat.requestPermissions(activity!!, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        recordButton()
        playButton()
        finishButton()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) popBackStack()
    }

    private fun finishButton() {
        button_finish_audio.setOnClickListener {
            finishAudio()
        }
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
        mStartPlaying = !mStartPlaying
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(audioFile.absolutePath)
            mPlayer!!.prepare()
            mPlayer!!.start()
            button_audio_play.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, activity?.theme))
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mPlayer?.setOnCompletionListener {
            onPlay(false)
        }
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
        button_audio_play.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, activity?.theme))
    }

    private fun startRecording() {
        // From https://stackoverflow.com/questions/5010145/very-poor-quality-of-audio-recorded-on-my-droidx-using-mediarecorder-why
        val bitDepth = 16
        val sampleRate = 44100
        val bitRate = sampleRate * bitDepth
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder!!.setAudioEncodingBitRate(bitRate)
        mRecorder!!.setAudioSamplingRate(sampleRate)
        mRecorder!!.setOutputFile(audioFile.absolutePath)

        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
        button_audio_record.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, activity?.theme))
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
        button_audio_record.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_black_24dp, activity?.theme))
    }

    override fun onStop() {
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
            mStartRecording = !mStartRecording
        }
    }

    private fun playButton() {
        button_audio_play.setOnClickListener {
            onPlay(mStartPlaying)
        }
    }

    private fun finishAudio() {
        val result = Intent()
        var resultData: Uri? = null
        try {
            resultData = Uri.fromFile(audioFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.i("uri", resultData!!.toString())
        result.data = resultData
        popBackStack()

    }

    companion object {
        private const val LOG_TAG = "AudioRecordActivity"
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}