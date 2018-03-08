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

package com.MapPost.ui.overview;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.MapPost.repository.PostRepository;
import com.MapPost.repository.tasks.StoryKey;
import com.MapPost.vo.Resource;
import com.MapPost.vo.Story;

import javax.inject.Inject;

public class OverviewViewModel extends ViewModel {
    @VisibleForTesting
    private final PostRepository postRepository;

    @Inject
    public OverviewViewModel(PostRepository repository) {
        this.postRepository = repository;
    }

    public LiveData<Resource<Story>> getStory(StoryKey storyKey) {
        return postRepository.getOneStory(storyKey, false);
    }
}
