package com.talkingwhale.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.talkingwhale.R
import kotlinx.android.synthetic.main.activity_container.*

class ContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        supportFragmentManager?.beginTransaction()
                ?.replace(container.id, SplashActivity())
                ?.commit()
    }
}
