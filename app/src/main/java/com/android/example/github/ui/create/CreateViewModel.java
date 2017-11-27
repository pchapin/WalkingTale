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

package com.android.example.github.ui.create;

import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.walkingTale.StoryCreateManager;

import javax.inject.Inject;

public class CreateViewModel extends ViewModel {


    public StoryCreateManager storyManager = new StoryCreateManager();
    private RepoRepository repoRepository;

    @Inject
    public CreateViewModel(RepoRepository repository) {
        this.repoRepository = repository;
    }

    void finishStory(Context context) {
        repoRepository.publishStory(storyManager.getStory(), context);
    }
}
