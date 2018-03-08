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

package com.MapPost.ui.profile;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.MapPost.repository.PostRepository;
import com.MapPost.repository.UserRepository;
import com.MapPost.util.AbsentLiveData;
import com.MapPost.vo.Resource;
import com.MapPost.vo.Story;
import com.MapPost.vo.User;

import java.util.List;

import javax.inject.Inject;

public class ProfileViewModel extends ViewModel {

    LiveData<Resource<User>> user = new MutableLiveData<>();
    LiveData<Resource<List<Story>>> playedStories = new MutableLiveData<>();
    LiveData<Resource<List<Story>>> createdStories = new MutableLiveData<>();
    boolean shouldFetch = false;
    private MutableLiveData<String> userId = new MutableLiveData<>();
    private PostRepository postRepository;

    @Inject
    ProfileViewModel(PostRepository repository, UserRepository userRepository) {
        postRepository = repository;
        user = Transformations.switchMap(userId, input -> {
            if (userId == null) return AbsentLiveData.create();
            else return userRepository.loadUser(input);
        });
    }

    void setUserId(String id) {
        userId.setValue(id);
    }

    LiveData<Resource<Story>> deleteStory(Story story) {
        return postRepository.deleteStory(story);
    }
}
