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
import com.android.example.github.api.RepoSearchResponse;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Status;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

/**
 * A task that uploads a created story to a remote database.
 */
public class FetchAllReposTask implements Runnable {
    private final GithubService githubService;
    private MutableLiveData<Resource<List<Repo>>> result = new MutableLiveData<>();


    FetchAllReposTask(GithubService githubService) {
        this.githubService = githubService;
        this.result.postValue(null);
    }


    @Override
    public void run() {

        try {
            Log.i("ddb", "Trying to get stories");
            Response<RepoSearchResponse> s = githubService.getAllRepos().execute();
            if (s.isSuccessful()) {
                result.postValue(new Resource<>(Status.SUCCESS, s.body().getItems(), s.message()));
                Log.i("ddb", "Get stories success" + s.message());
            } else {
                Log.i("ddb", "Get stories failed: " + s.errorBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Resource<List<Repo>>> getLiveData() {
        return result;
    }
}
