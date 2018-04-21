package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.talkingwhale.R
import com.talkingwhale.pojos.Status

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AWSMobileClient.getInstance().initialize(this@SplashActivity) {
            Handler().postDelayed({
                val identityManager = IdentityManager.getDefaultIdentityManager()
                identityManager.resumeSession(this@SplashActivity, { authResults ->
                    if (authResults.isUserSignedIn) {
                        AWSLoginModel.getUserId(this).observe(this, Observer {
                            if (it?.status == Status.SUCCESS) {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            }
                        })
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }, 3000)
            }, 3000)
        }.execute()
    }
}
