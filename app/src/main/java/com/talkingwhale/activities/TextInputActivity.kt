package com.talkingwhale.activities

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityTextInputBinding
import kotlinx.android.synthetic.main.activity_text_input.*

class TextInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextInputBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_text_input)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        postTextButton()
    }

    private fun postTextButton() {
        button_text_post_submit.setOnClickListener {
            val text = binding.postTextEditText.text.toString().trim()
            if (text.isBlank()) {
                Toast.makeText(this, "Text cannot be blank", Toast.LENGTH_SHORT).show()
                post_text_edit_text.setText("")
                return@setOnClickListener
            }

            val intent = Intent()
            intent.putExtra(TEXT_KEY, text)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    companion object {
        const val TEXT_KEY = "TEXT_KEY"
    }
}
