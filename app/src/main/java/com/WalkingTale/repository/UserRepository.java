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

package com.WalkingTale.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.WalkingTale.AppExecutors;
import com.WalkingTale.api.WalkingTaleService;
import com.WalkingTale.db.UserDao;
import com.WalkingTale.db.WalkingTaleDb;
import com.WalkingTale.repository.tasks.GetUserTask;
import com.WalkingTale.repository.tasks.PutUserTask;
import com.WalkingTale.vo.Resource;
import com.WalkingTale.vo.User;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository that handles User objects.
 */
@Singleton
public class UserRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final UserDao userDao;
    private final WalkingTaleService walkingTaleService;
    private final AppExecutors appExecutors;
    private final WalkingTaleDb db;

    @Inject
    UserRepository(AppExecutors appExecutors, UserDao userDao, WalkingTaleService walkingTaleService, WalkingTaleDb db) {
        this.userDao = userDao;
        this.walkingTaleService = walkingTaleService;
        this.appExecutors = appExecutors;
        this.db = db;
    }

    public LiveData<Resource<User>> loadUser(String userId) {
        return new NetworkBoundResource<User, User>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull User item) {
                userDao.insert(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable User data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<User> loadFromDb() {
                return userDao.findByLogin(userId);
            }

            @NonNull
            @Override
            protected LiveData<Resource<User>> createCall() {
                GetUserTask getUserTask = new GetUserTask(userId, db);
                appExecutors.networkIO().execute(getUserTask);
                return getUserTask.getResult();
            }
        }.asLiveData();
    }

    public LiveData<Resource<Void>> putUser(User user) {
        PutUserTask putUserTask = new PutUserTask(user, db);
        appExecutors.networkIO().execute(putUserTask);
        return putUserTask.getResult();
    }
}
