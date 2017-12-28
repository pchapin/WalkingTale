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

package com.android.example.github.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.android.example.github.api.GithubService;
import com.android.example.github.vo.Repo;

import java.io.IOException;

import retrofit2.Response;

/**
 * A task that uploads a created story to a remote database.
 */
public class SaveStoryTask implements Runnable {
    private final GithubService githubService;
    private Repo story;
    private MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();


    SaveStoryTask(Repo story, GithubService githubService) {
        this.story = story;
        this.githubService = githubService;
        this.isSuccessful.postValue(false);
    }


    @Override
    public void run() {

        try {
            Log.i("ddb", "Trying to publish story: " + story);
            Log.i("repo", story.toJson());
            Response<Repo> s = githubService.putRepo(story).execute();
            if (s.isSuccessful()) {
                isSuccessful.postValue(true);
                Log.i("ddb", "Publish story success: " + s.body());
            } else {
                isSuccessful.postValue(false);
                Log.i("ddb", "Publish story failed: " + s.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Boolean> getLiveData() {
        return isSuccessful;
    }
}
