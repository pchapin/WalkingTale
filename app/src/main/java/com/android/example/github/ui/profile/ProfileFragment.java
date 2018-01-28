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

package com.android.example.github.ui.profile;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.android.example.github.MainActivity;
import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.FragmentProfileBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.RepoListAdapter;
import com.android.example.github.util.AutoClearedValue;

import javax.inject.Inject;

/**
 * The UI Controller for displaying the overview for a story.
 */
public class ProfileFragment extends LifecycleFragment implements LifecycleRegistryOwner, Injectable {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<FragmentProfileBinding> binding;
    AutoClearedValue<RepoListAdapter> adapter;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentProfileBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_profile, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @NonNull
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profileViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel.class);

        RepoListAdapter repoListAdapter = new RepoListAdapter(dataBindingComponent, false, repo -> {
            profileViewModel.setUserId(repo.username);
        });
        binding.get().repoList.setAdapter(repoListAdapter);
        adapter = new AutoClearedValue<>(this, repoListAdapter);

        profileViewModel.setUserId(MainActivity.cognitoId);
        profileViewModel.user.observe(this, userResource -> {
            if (userResource != null) binding.get().setUser(userResource.data);
        });

        initRecyclerView();
        initTabHost();
    }

    private void initTabHost() {
        TabHost tabHost = binding.get().tabs;
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third Tab");

        tab1.setIndicator("Played");
        tab1.setContent(binding.get().tab1.getId());

        tab2.setIndicator("Created");
        tab2.setContent(binding.get().tab2.getId());

        tab3.setIndicator("Following");
        tab3.setContent(binding.get().tab3.getId());

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        initTabListener();
    }

    private void initTabListener() {
        binding.get().tabs.setOnTabChangedListener(s -> {
            switch (binding.get().tabs.getCurrentTab()) {
                case 0:
                    break;
            }
        });
    }

    private void initRecyclerView() {
        profileViewModel.usersRepos.observe(this, result -> {
            if (result != null && result.data != null) {
                adapter.get().replace(result.data);
            }
            binding.get().executePendingBindings();
        });
    }


}
