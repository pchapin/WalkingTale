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

import com.android.example.github.AppExecutors;
import com.android.example.github.api.GithubService;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.StoryDao;
import com.android.example.github.repository.tasks.GetAllStoriesTask;
import com.android.example.github.repository.tasks.GetOneStoryTask;
import com.android.example.github.repository.tasks.PutFileS3Task;
import com.android.example.github.repository.tasks.S3Args;
import com.android.example.github.repository.tasks.SaveStoryTask;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Story;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoryRepository {

    private final String TAG = this.getClass().getSimpleName();
    private final GithubDb db;
    private final StoryDao storyDao;
    private final GithubService githubService;
    private final AppExecutors appExecutors;

    @Inject
    public StoryRepository(AppExecutors appExecutors, GithubDb db, StoryDao storyDao,
                           GithubService githubService) {
        this.db = db;
        this.storyDao = storyDao;
        this.githubService = githubService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<Void>> publishStory(Story story) {
        SaveStoryTask saveStoryTask = new SaveStoryTask(story, db);
        appExecutors.networkIO().execute(saveStoryTask);
        return saveStoryTask.getResult();
    }

    public LiveData<Resource<List<Story>>> getAllStories() {
        GetAllStoriesTask getAllStoriesTask = new GetAllStoriesTask("", db);
        appExecutors.networkIO().execute(getAllStoriesTask);
        return getAllStoriesTask.getResult();
    }

    public LiveData<Resource<Story>> getOneStory(String id) {
        GetOneStoryTask getOneStoryTask = new GetOneStoryTask(id, db);
        appExecutors.networkIO().execute(getOneStoryTask);
        return getOneStoryTask.getResult();
    }

    public LiveData<Resource<Story>> putFileInS3(S3Args s3Args) {
        PutFileS3Task putFileS3Task = new PutFileS3Task(s3Args, db);
        appExecutors.networkIO().execute(putFileS3Task);
        return putFileS3Task.getResult();
    }
}
