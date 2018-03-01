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

package com.walkingtale.ui.profile;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.walkingtale.repository.StoryRepository;
import com.walkingtale.repository.UserRepository;
import com.walkingtale.util.AbsentLiveData;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.Story;
import com.walkingtale.vo.User;

import java.util.List;

import javax.inject.Inject;

public class ProfileViewModel extends ViewModel {

    LiveData<Resource<User>> user = new MutableLiveData<>();
    LiveData<Resource<List<Story>>> usersRepos = new MutableLiveData<>();
    private MutableLiveData<String> userId = new MutableLiveData<>();
    private StoryRepository storyRepository;

    @Inject
    ProfileViewModel(StoryRepository repository, UserRepository userRepository) {
        storyRepository = repository;
        user = Transformations.switchMap(userId, input -> {
            if (userId == null) return AbsentLiveData.create();
            else return userRepository.loadUser(input);
        });

        usersRepos = Transformations.switchMap(userId, input -> {
            if (userId == null) return AbsentLiveData.create();
                // TODO: 1/28/18 only get the repos for a single user
            else return repository.getAllStories();
        });
    }

    void setUserId(String id) {
        userId.setValue(id);
    }

    LiveData<Resource<Story>> deleteStory(Story story) {
        return storyRepository.deleteStory(story);
    }

    LiveData<Resource<List<Story>>> getPlayedStories() {
        return storyRepository.getPlayedStories(user.getValue().data);
    }
}
