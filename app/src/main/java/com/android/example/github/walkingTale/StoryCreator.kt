package com.android.example.github.walkingTale

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.example.github.R
import android.arch.lifecycle.ViewModelProviders




class StoryCreator : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_creator)
        val mViewModel = ViewModelProviders.of(this).get(StoryCreatorViewModel::class.java)

    }

    fun createChapter(view: View) {
        val intent = Intent(this, ChapterCreator::class.java)
//        val editText = findViewById<View>(R.id.editText) as EditText
//        val message = editText.text.toString()
//        intent.putExtra(EXTRA_MESSAGE, message)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent) {
        Log.i("chapter return value", "$resultCode ${Activity.RESULT_OK}")
        if (resultCode == Activity.RESULT_OK) {
            val myValue = data.getStringExtra("valueName")
            // use 'myValue' return value here
            Toast.makeText(this, myValue, Toast.LENGTH_SHORT).show()
        }
    }

    fun reviewStory(view: View) {
        Toast.makeText(this, "todo: create review activity", Toast.LENGTH_SHORT).show()
    }


}
