package com.MapPost

import android.app.Activity
import android.os.Bundle

import com.amazonaws.mobile.auth.ui.SignInUI
import com.amazonaws.mobile.client.AWSMobileClient

class AuthenticatorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticator)

        AWSMobileClient.getInstance().initialize(this) {
            val signin = AWSMobileClient
                    .getInstance()
                    .getClient(this@AuthenticatorActivity, SignInUI::class.java) as SignInUI
            signin.login(this@AuthenticatorActivity, MainActivity::class.java).execute()
        }.execute()
    }
}