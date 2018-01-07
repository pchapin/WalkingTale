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

package com.android.example.github.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.example.github.api.GithubService
import com.android.example.github.db.GithubDb
import com.android.example.github.db.RepoDao
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import java.io.IOException
import java.util.*

/**
 * A task that uploads a created story to a remote database.
 */
class FetchAllReposTask internal constructor(private val githubService: GithubService, private val githubDb: GithubDb, private val repoDao: RepoDao) : Runnable {

    private val TAG = this.javaClass.simpleName
    private val result = MutableLiveData<Resource<List<Repo>>>()
    val liveData: LiveData<Resource<List<Repo>>>
        get() = result

    init {
        this.result.postValue(Resource(Status.LOADING, null, ""))
    }

    override fun run() {

        try {
            Log.i(TAG, "Trying to get stories")
            // todo: pass cognito auth token
            val s = githubService.getAllRepos("").execute()
            Log.i(TAG, "Get stories result: " + s.body())
            if (s.isSuccessful) {
                githubDb.beginTransaction()
                repoDao.insertRepos(s.body()!!.items)
                githubDb.setTransactionSuccessful()
                githubDb.endTransaction()
                result.postValue(Resource(Status.SUCCESS, s.body()!!.items, s.message()))
            } else {
                result.postValue(Resource(Status.ERROR, ArrayList(), s.message()))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}