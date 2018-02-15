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
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.example.github.AppExecutors;
import com.android.example.github.MainActivity;
import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GithubService;
import com.android.example.github.api.RepoSearchResponse;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.util.RateLimiter;
import com.android.example.github.vo.RepoSearchResult;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.Story;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository that handles Story instances.
 * <p>
 * unfortunate naming :/ .
 * Story - value object name
 * Repository - type of this class.
 */
@Singleton
public class RepoRepository {

    private final String TAG = this.getClass().getSimpleName();
    private final GithubDb db;
    private final RepoDao repoDao;
    private final GithubService githubService;
    private final AppExecutors appExecutors;
    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public RepoRepository(AppExecutors appExecutors, GithubDb db, RepoDao repoDao,
                          GithubService githubService) {
        this.db = db;
        this.repoDao = repoDao;
        this.githubService = githubService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Boolean> publishStory(Story story) {
        SaveStoryTask saveStoryTask = new SaveStoryTask(story, githubService);
        appExecutors.networkIO().execute(saveStoryTask);
        return saveStoryTask.getLiveData();
    }

    public LiveData<Resource<List<Story>>> getAllRepos(boolean shouldFetch) {
        GetStoriesTask getStoriesTask = new GetStoriesTask(githubService);
        appExecutors.networkIO().execute(getStoriesTask);

        return new NetworkBoundResource<List<Story>, List<Story>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Story> item) {
                repoDao.insertRepos(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Story> data) {
                boolean result = shouldFetch || data == null || data.isEmpty();
                Log.i(TAG, "fetching new stories: " + result);
                return result;
            }

            @NonNull
            @Override
            protected LiveData<List<Story>> loadFromDb() {
                return repoDao.loadAll();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Story>>> createCall() {
                return githubService.getAllRepos(MainActivity.cognitoToken);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Story>> loadRepo(String id) {
        return new NetworkBoundResource<Story, Story>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Story item) {
                repoDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Story data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Story> loadFromDb() {
                return repoDao.load(id);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Story>> createCall() {
                return githubService.getRepo(id);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> searchNextPage(String query) {
        // TODO: 1/17/18 search with query
        return null;
    }

    public LiveData<Resource<List<Story>>> search(String query) {
        return new NetworkBoundResource<List<Story>, RepoSearchResponse>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull RepoSearchResponse item) {
                List<String> repoIds = item.getRepoIds();
                RepoSearchResult repoSearchResult = new RepoSearchResult(
                        query, repoIds, item.getTotal(), item.getNextPage());
                db.beginTransaction();
                try {
                    repoDao.insertRepos(item.getItems());
                    repoDao.insert(repoSearchResult);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Story> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<List<Story>> loadFromDb() {
                return Transformations.switchMap(repoDao.search(query), searchData -> {
                    if (searchData == null) {
                        return AbsentLiveData.create();
                    } else {
//                        return repoDao.loadOrdered(searchData.repoIds);
                        return repoDao.loadAll();
                    }
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RepoSearchResponse>> createCall() {
                // TODO: 1/17/18 search with query
                return null;
            }

            @Override
            protected RepoSearchResponse processResponse(ApiResponse<RepoSearchResponse> response) {
                RepoSearchResponse body = response.getBody();
                if (body != null) {
//                    body.setNextPage(response.getNextPage());
                }
                return body;
            }
        }.asLiveData();
    }
}
