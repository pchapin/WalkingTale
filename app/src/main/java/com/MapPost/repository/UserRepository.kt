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

package com.MapPost.repository

import android.arch.lifecycle.LiveData
import com.MapPost.AppExecutors
import com.MapPost.db.AppDatabase
import com.MapPost.db.UserDao
import com.MapPost.repository.tasks.GetUserTask
import com.MapPost.repository.tasks.PutUserTask
import com.MapPost.vo.Resource
import com.MapPost.vo.User

/**
 * Repository that handles User objects.
 */
class UserRepository internal constructor(private val appExecutors: AppExecutors, private val userDao: UserDao, private val db: AppDatabase) {
    private val TAG = this.javaClass.simpleName

    fun loadUser(userId: String): LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, User>(appExecutors) {
            override fun saveCallResult(item: User) {
                userDao.insert(item)
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<User> {
                return userDao.findByLogin(userId)
            }

            override fun createCall(): LiveData<Resource<User>> {
                val getUserTask = GetUserTask(userId, db)
                appExecutors.networkIO().execute(getUserTask)
                return getUserTask.result
            }
        }.asLiveData()
    }

    fun putUser(user: User): LiveData<Resource<Void>> {
        val putUserTask = PutUserTask(user, db)
        appExecutors.networkIO().execute(putUserTask)
        return putUserTask.result
    }
}
