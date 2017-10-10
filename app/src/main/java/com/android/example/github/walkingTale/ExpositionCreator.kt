package com.android.example.github.walkingTale

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.app.Activity
import android.util.Log
import android.widget.EditText
import com.android.example.github.R


class ExpositionCreator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exposition_creator)
    }

    fun returnExposition(view: View) {
        val resultData = Intent()
        val edit = findViewById<EditText>(R.id.exposition_text)
        val text = edit.text.toString()
        Log.i("exposition value", text)
        resultData.putExtra("valueName", text)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }
}
