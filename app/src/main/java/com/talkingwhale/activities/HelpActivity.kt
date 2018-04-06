package com.talkingwhale.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.talkingwhale.R

import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
