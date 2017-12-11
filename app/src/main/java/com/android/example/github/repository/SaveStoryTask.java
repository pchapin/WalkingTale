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
import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GithubService;
import com.android.example.github.api.RepoSearchResponse;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.ExampleRepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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
                Log.i("ddb", "Publish story success" + s.message());
            } else {
                isSuccessful.postValue(false);
                Log.i("ddb", "Publish story failed: " + s.errorBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Boolean> getLiveData() {
        return isSuccessful;
    }
}
