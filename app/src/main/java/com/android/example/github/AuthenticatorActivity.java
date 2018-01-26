package com.android.example.github;

import android.app.Activity;
import android.os.Bundle;

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;

public class AuthenticatorActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        AWSMobileClient.getInstance().initialize(this, awsStartupResult -> {
            SignInUI signin = (SignInUI) AWSMobileClient
                    .getInstance()
                    .getClient(AuthenticatorActivity.this, SignInUI.class);
            signin.login(AuthenticatorActivity.this, MainActivity.class).execute();
        }).execute();
    }
}