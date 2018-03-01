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

package com.walkingtale.repository;

import android.arch.lifecycle.LiveData;

import com.walkingtale.AppExecutors;
import com.walkingtale.api.WalkingTaleService;
import com.walkingtale.db.StoryDao;
import com.walkingtale.db.WalkingTaleDb;
import com.walkingtale.repository.tasks.DeleteStoryTask;
import com.walkingtale.repository.tasks.GetAllStoriesTask;
import com.walkingtale.repository.tasks.GetOneStoryTask;
import com.walkingtale.repository.tasks.GetPlayedStoriesTask;
import com.walkingtale.repository.tasks.PutFileS3Task;
import com.walkingtale.repository.tasks.S3Args;
import com.walkingtale.repository.tasks.SaveStoryTask;
import com.walkingtale.repository.tasks.StoryKey;
import com.walkingtale.vo.Resource;
import com.walkingtale.vo.Story;
import com.walkingtale.vo.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoryRepository {

    private final String TAG = this.getClass().getSimpleName();
    private final WalkingTaleDb db;
    private final StoryDao storyDao;
    private final WalkingTaleService walkingTaleService;
    private final AppExecutors appExecutors;

    @Inject
    public StoryRepository(AppExecutors appExecutors, WalkingTaleDb db, StoryDao storyDao,
                           WalkingTaleService walkingTaleService) {
        this.db = db;
        this.storyDao = storyDao;
        this.walkingTaleService = walkingTaleService;
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

    public LiveData<Resource<Story>> getOneStory(StoryKey storyKey) {
        GetOneStoryTask getOneStoryTask = new GetOneStoryTask(storyKey, db);
        appExecutors.networkIO().execute(getOneStoryTask);
        return getOneStoryTask.getResult();
    }

    public LiveData<Resource<Story>> putFileInS3(S3Args s3Args) {
        PutFileS3Task putFileS3Task = new PutFileS3Task(s3Args, db);
        appExecutors.networkIO().execute(putFileS3Task);
        return putFileS3Task.getResult();
    }

    public LiveData<Resource<Story>> deleteStory(Story story) {
        DeleteStoryTask deleteStoryTask = new DeleteStoryTask(story, db);
        appExecutors.networkIO().execute(deleteStoryTask);
        return deleteStoryTask.getResult();
    }

    public LiveData<Resource<List<Story>>> getPlayedStories(User user) {
        GetPlayedStoriesTask getPlayedStoriesTask = new GetPlayedStoriesTask(user, db);
        appExecutors.networkIO().execute(getPlayedStoriesTask);
        return getPlayedStoriesTask.getResult();
    }
}
