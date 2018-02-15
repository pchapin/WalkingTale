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

package com.android.example.github;

import android.app.Dialog;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.PermissionManager;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends AppCompatActivity implements
        LifecycleRegistryOwner,
        HasSupportFragmentInjector {

    private final String TAG = this.getClass().getSimpleName();
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    NavigationController navigationController;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    Dialog playServicesErrorDialog;
    private MainViewModel mainViewModel;

    public static String getCognitoToken() {
        return IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider().getToken();
    }

    public static String getCognitoId() {
        return IdentityManager.getDefaultIdentityManager().getCachedUserID();
    }

    public static String getCognitoUsername() {
        return new JWT(getCognitoToken()).getClaim("cognito:username").asString();
    }

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        userSetup(savedInstanceState);
        initBottomNavigationListener();
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private void userSetup(Bundle savedInstanceState) {

        mainViewModel.getUser(IdentityManager.getDefaultIdentityManager().getCachedUserID()).observe(this, userResource -> {

        });

////        mainViewModel.user.observe(this, userResource -> {
////            if (userResource != null) {
////                switch (userResource.status) {
////                    case SUCCESS:
////                        Log.i(TAG, "Get user success " + userResource);
////                        if (savedInstanceState == null) navigationController.navigateToStoryFeed();
////                        mainViewModel.user.removeObservers(this);
////                        mainViewModel.newUser.removeObservers(this);
////                        break;
////                    case LOADING:
////                        Log.i(TAG, "Get user loading " + userResource);
////                        break;
////                    case ERROR:
////                        Log.i(TAG, "Get user error " + userResource);
////                        // Create new user if user not already in user db
////                        if (userResource.message != null && userResource.message.contains("User not found in")) {
////                            mainViewModel.setCreateNewUser(new User(cognitoId, new ArrayList<>(), new ArrayList<>(), cognitoUsername, "none"));
////                        }
////                        break;
////                }
////            }
////        });
//
//        mainViewModel.newUser.observe(this, userResource -> {
//            if (userResource != null && userResource.data != null) {
//                Log.i(TAG, "Creating new user: " + userResource);
//                mainViewModel.setUserId(userResource.data.userId);
//            }
//        });
//
//        mainViewModel.setUserId(cognitoId);
    }

    private void initBottomNavigationListener() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    navigationController.navigateToStoryFeed();
                    break;
                case R.id.action_search:
                    navigationController.navigateToSearch();
                    break;
                case R.id.action_create:
                    if (PermissionManager.checkLocationPermission(this))
                        navigationController.navigateToCreateStory();
                    break;
                case R.id.action_profile:
                    navigationController.navigateToProfile();
                    break;
            }
            return true;
        });
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
}
