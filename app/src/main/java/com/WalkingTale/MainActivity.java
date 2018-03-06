/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.WalkingTale;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.WalkingTale.ui.common.NavigationController;
import com.WalkingTale.vo.User;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {
    public static final DEBUG_STATE DEBUG_MODE = DEBUG_STATE.OFF;
    public static final String SP_USER_ID_KEY = "SP_USER_ID_KEY";
    public static final String SP_USERNAME_KEY = "SP_USERNAME_KEY";
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    NavigationController navigationController;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    Dialog playServicesErrorDialog;
    private MainViewModel mainViewModel;

    public static String getCognitoId() {
        return IdentityManager.getDefaultIdentityManager().getCachedUserID();
    }

    public static String getCognitoUsername() {
        String cognitoToken = IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider().getToken();
        JWT jwt = new JWT(cognitoToken);
        String username = jwt.getClaim("cognito:username").asString();
        if (username == null) {
            return jwt.getClaim("given_name").asString();
        } else {
            return username;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        // Obtain the FirebaseAnalytics instance.
        Analytics.INSTANCE.init(this);
        userSetup(savedInstanceState);
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private void userSetup(Bundle savedInstanceState) {

        mainViewModel.getUser(getCognitoId()).observe(this, userResource -> {
            if (userResource != null) {
                switch (userResource.status) {
                    case ERROR:
                        createNewUser();
                        break;
                    case LOADING:
                        break;
                    case SUCCESS:
                        Analytics.INSTANCE.logEvent(Analytics.EventType.UserLogin, TAG);
                        if (savedInstanceState == null) {
                            navigationController.navigateToStoryFeed();
                            break;
                        }
                }
            }
        });
    }

    private void createNewUser() {

        mainViewModel.createUser(new User(
                getCognitoId(),
                new ArrayList<>(),
                new ArrayList<>(),
                getCognitoUsername(),
                "none"
        )).observe(this, newUserResource -> {
            if (newUserResource != null) {
                switch (newUserResource.status) {
                    case SUCCESS:
                        Analytics.INSTANCE.logEvent(Analytics.EventType.CreatedUser, TAG);
                        navigationController.navigateToStoryFeed();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    /**
     * Prevents user from using the app unless they have google play services installed.
     * Not having it will prevent the google map from working.
     */
    private void checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    playServicesErrorDialog.setCancelable(false);
                }

                if (!playServicesErrorDialog.isShowing())
                    playServicesErrorDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    public enum DEBUG_STATE {
        OFF, CREATE, PLAY, PROFILE
    }
}