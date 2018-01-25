package com.android.example.github;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.android.example.github.aws.AWSProvider;
import com.android.example.github.aws.CognitoLogin;
import com.auth0.android.jwt.JWT;

import java.util.concurrent.ExecutionException;

public class AuthenticatorActivity extends AppCompatActivity {

    static final String COGNITO_TOKEN_KEY = "COGNITO_TOKEN_KEY";
    static final String COGNITO_USERNAME_KEY = "COGNITO_USERNAME_KEY";

    final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        // Initialize the AWS Provider
        AWSProvider.initialize(getApplicationContext());


        final IdentityManager identityManager = AWSProvider.getInstance().getIdentityManager();
        // Set up the callbacks to handle the authentication response
        identityManager.setUpToAuthenticate(this, new DefaultSignInResultHandler() {

            @Override
            public void onSuccess(Activity activity, IdentityProvider identityProvider) {
                String cognitoToken = identityProvider.getToken();
                Log.i(TAG, cognitoToken);

                // Decode token here to see what the fields are: https://jwt.io/
                String username = new JWT(cognitoToken).getClaim("cognito:username").asString();

                Toast.makeText(AuthenticatorActivity.this,
                        String.format("Logged in as %s", identityManager.getCachedUserID()),
                        Toast.LENGTH_LONG).show();
                // Go to the main activity
                final Intent intent = new Intent(activity, MainActivity.class)
                        .putExtra(COGNITO_TOKEN_KEY, cognitoToken)
                        .putExtra(COGNITO_USERNAME_KEY, username)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.finish();
            }

            @Override
            public boolean onCancel(Activity activity) {
                return false;
            }
        });


        // TODO: 1/19/18 development only
        Context context = this;
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                return new CognitoLogin().getToken(context);
            }
        };
        String cognitoToken = null;
        try {
            cognitoToken = (String) asyncTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        String username = new JWT(cognitoToken).getClaim("cognito:username").asString();
        final Intent intent = new Intent(this, MainActivity.class)
                .putExtra(COGNITO_TOKEN_KEY, cognitoToken)
                .putExtra(COGNITO_USERNAME_KEY, username)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();


        // TODO: 1/19/18 uncomment, or add a better id screen
//        // Start the authentication UI
//        AuthUIConfiguration config = new AuthUIConfiguration.Builder()
//                .userPools(true)
//                .build();
//        SignInActivity.startSignInActivity(this, config);
//        AuthenticatorActivity.this.finish();
    }
}
