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

package com.android.example.github.ui.chaptercreate;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.CreateChapterFragmentBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.repo.ContributorAdapter;
import com.android.example.github.ui.storycreate.StoryCreateFragment;
import com.android.example.github.util.AutoClearedValue;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import javax.inject.Inject;

/**
 * The UI Controller for displaying a Github Repo's information with its contributors.
 */
public class ChapterCreateFragment extends Fragment implements LifecycleRegistryOwner, Injectable {

    private static final String REPO_OWNER_KEY = "repo_owner";

    private static final String REPO_NAME_KEY = "repo_name";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);


    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    AutoClearedValue<CreateChapterFragmentBinding> binding;
    AutoClearedValue<ContributorAdapter> adapter;
    private ChapterCreateViewModel ChapterCreateViewModel;

    public static ChapterCreateFragment create(String owner, String name) {
        ChapterCreateFragment repoFragment = new ChapterCreateFragment();
        Bundle args = new Bundle();
        args.putString(REPO_OWNER_KEY, owner);
        args.putString(REPO_NAME_KEY, name);
        repoFragment.setArguments(args);
        return repoFragment;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChapterCreateViewModel = ViewModelProviders.of(this, viewModelFactory).get(ChapterCreateViewModel.class);
        Bundle args = getArguments();

        ContributorAdapter adapter = new ContributorAdapter(dataBindingComponent,
                contributor -> navigationController.navigateToUser(contributor.getLogin()));
        this.adapter = new AutoClearedValue<>(this, adapter);
        initPictureExpositionListener();
        initTextExpositionListener();
        initAudioExpositionListener();
        initFinishChapterListener();
        getActivity().setTitle("Create Chapter");

    }

    private void initPictureExpositionListener() {
        binding.get().addPictureExposition.setOnClickListener((v) -> {
            //Todo: add picture exposition
        });
    }

    private void initTextExpositionListener() {
        binding.get().addTextExposition.setOnClickListener((v) -> {
            Editable textExposition = binding.get().textExpositionEdittext.getText();

            if (TextUtils.isEmpty(textExposition)) {
                //Display error: text exposition cannot be empty
                Toast.makeText(getActivity(), "Text exposition cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                ChapterCreateViewModel.addTextExposition(textExposition);

                Toast.makeText(getActivity(), ChapterCreateViewModel.getChapter().toString(), Toast.LENGTH_LONG).show();

                TextView textView = new TextView(getActivity());
                textView.setText(textExposition);
                LinearLayout linearLayout = this.binding.get().chapterLinearLayout;
                linearLayout.addView(textView);
            }
        });
    }

    private void initAudioExpositionListener() {
        binding.get().addAudioExposition.setOnClickListener((v) -> {
            //Todo: add audio exposition
        });
    }

    private void initFinishChapterListener() {
        binding.get().finishChapter.setOnClickListener((v) -> {
            //Todo: If chapter info is valid, add chapter to story
        });
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CreateChapterFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.create_chapter_fragment, container, false);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }
}
