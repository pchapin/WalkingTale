package com.android.example.github.walkingTale

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.example.github.R
import java.util.ArrayList

class ChapterCreator : AppCompatActivity() {

    val chapters = ArrayList<Chapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_creator)
    }

    fun createExposition(view: View) {
        val intent = Intent(this, ExpositionCreator::class.java)
        startActivityForResult(intent, 1)
    }


    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent) {
        Log.i("exposition return value", "$resultCode ${Activity.RESULT_OK}")
        if (resultCode == Activity.RESULT_OK) {
            val myValue = data.getStringExtra("valueName")
            // use 'myValue' return value here
            Toast.makeText(this, myValue, Toast.LENGTH_SHORT).show()


            // Append exposition text
            val chapterLinearLayout = findViewById<LinearLayout>(R.id.chapter_linear_layout)
            val chapterView = TextView(this)
            chapterView.text = myValue
            chapterLinearLayout.addView(chapterView)
        }
    }

    fun returnChapter(view: View) {
        val resultData = Intent()
        resultData.putExtra("valueName", "todo: attach chapters")
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

}
