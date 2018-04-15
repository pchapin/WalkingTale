package com.talkingwhale.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration
import com.amazonaws.mobile.auth.ui.SignInUI
import com.amazonaws.mobile.client.AWSMobileClient
import com.talkingwhale.R

class AuthenticatorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator)

        AWSMobileClient.getInstance().initialize(this) {
            val config = AuthUIConfiguration.Builder()
                    .userPools(true)  // true? show the Email and Password UI
                    //todo: change background image
                    .logoResId(R.drawable.ic_directions_walk_black_24dp) // Change the logo
                    .backgroundColor(Color.WHITE) // Change the backgroundColor
                    .isBackgroundColorFullScreen(false) // Full screen backgroundColor the backgroundColor full screen
                    .canCancel(true)
                    .build()
            val signinUI = AWSMobileClient.getInstance().getClient(this@AuthenticatorActivity, SignInUI::class.java) as SignInUI
            signinUI.login(this@AuthenticatorActivity, MainActivity::class.java).authUIConfiguration(config).execute()
        }.execute()
    }
}