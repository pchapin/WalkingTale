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
import com.android.example.github.db.UserDao;
import com.android.example.github.repository.tasks.GetUserTask;
import com.android.example.github.repository.tasks.PutUserTask;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository that handles User objects.
 */
@Singleton
public class UserRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final UserDao userDao;
    private final GithubService githubService;
    private final AppExecutors appExecutors;

    @Inject
    UserRepository(AppExecutors appExecutors, UserDao userDao, GithubService githubService) {
        this.userDao = userDao;
        this.githubService = githubService;
        this.appExecutors = appExecutors;
    }

    public LiveData<Resource<User>> loadUser(String userId) {
        GetUserTask getUserTask = new GetUserTask(userId);
        appExecutors.networkIO().execute(getUserTask);
        return getUserTask.getResult();
    }

    public LiveData<Resource<Void>> putUser(User user) {
        PutUserTask putUserTask = new PutUserTask(user);
        appExecutors.networkIO().execute(putUserTask);
        return putUserTask.getResult();
    }
}
