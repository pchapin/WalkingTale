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

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends AppCompatActivity implements
        LifecycleRegistryOwner,
        HasSupportFragmentInjector {

    public static String cognitoToken;
    public static String cognitoId;
    public static String cognitoUsername;

    private final String TAG = this.getClass().getSimpleName();
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    NavigationController navigationController;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private MainViewModel mainViewModel;


    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cognitoToken = IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider().getToken();
        cognitoId = IdentityManager.getDefaultIdentityManager().getCachedUserID();
        cognitoUsername = new JWT(cognitoToken).getClaim("cognito:username").asString();

        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        initBottomNavigationListener();

        if (savedInstanceState == null) {
            navigationController.navigateToProfile();
        }
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
}
