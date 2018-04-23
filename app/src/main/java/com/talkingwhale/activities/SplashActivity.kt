package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.talkingwhale.R
import com.talkingwhale.pojos.Status

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AWSMobileClient.getInstance().initialize(this) {
            val identityManager = IdentityManager.getDefaultIdentityManager()
            identityManager.resumeSession(this, { authResults ->
                if (authResults.isUserSignedIn) {
                    AWSLoginModel.getUserId(this).observe(this, Observer {
                        if (it?.status == Status.SUCCESS) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    })
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }, 1000)
        }.execute()
    }
}
