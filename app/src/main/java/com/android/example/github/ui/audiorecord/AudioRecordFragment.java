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

package com.android.example.github.ui.audiorecord;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentDataBindingComponent;
import com.android.example.github.databinding.AudioRecordBinding;
import com.android.example.github.di.Injectable;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.ui.common.RepoListAdapter;
import com.android.example.github.util.AutoClearedValue;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import javax.inject.Inject;

public class AudioRecordFragment extends LifecycleFragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    AutoClearedValue<AudioRecordBinding> binding;

    AutoClearedValue<RepoListAdapter> adapter;

    private AudioRecordViewModel AudioRecordViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AudioRecordBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.audio_record, container, false,
                        dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AudioRecordViewModel = ViewModelProviders.of(this, viewModelFactory).get(AudioRecordViewModel.class);
        initRecordAudioListener();
        initPlayAudioListener();
        initFinishListener();
        getActivity().setTitle("Audio Record");
    }

    private void initRecordAudioListener() {
        binding.get().recordAudioButton.setOnClickListener((v) -> {
        });
    }

    private void initPlayAudioListener() {
        binding.get().playAudioButton.setOnClickListener((v) -> {
        });
    }

    private void initFinishListener() {
        binding.get().finishButton.setOnClickListener((v) -> {
        });
    }
}
