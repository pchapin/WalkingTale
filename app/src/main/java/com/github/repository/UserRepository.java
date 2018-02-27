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

package com.github.repository;

import android.arch.lifecycle.LiveData;

import com.github.AppExecutors;
import com.github.api.GithubService;
import com.github.db.GithubDb;
import com.github.db.UserDao;
import com.github.repository.tasks.GetUserTask;
import com.github.repository.tasks.PutUserTask;
import com.github.vo.Resource;
import com.github.vo.User;

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
    private final GithubDb db;

    @Inject
    UserRepository(AppExecutors appExecutors, UserDao userDao, GithubService githubService, GithubDb db) {
        this.userDao = userDao;
        this.githubService = githubService;
        this.appExecutors = appExecutors;
        this.db = db;
    }

    public LiveData<Resource<User>> loadUser(String userId) {
        GetUserTask getUserTask = new GetUserTask(userId, db);
        appExecutors.networkIO().execute(getUserTask);
        return getUserTask.getResult();
    }

    public LiveData<Resource<Void>> putUser(User user) {
        PutUserTask putUserTask = new PutUserTask(user, db);
        appExecutors.networkIO().execute(putUserTask);
        return putUserTask.getResult();
    }
}
