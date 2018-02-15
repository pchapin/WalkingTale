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
import com.android.example.github.MainActivity
import com.android.example.github.api.GithubService
import com.android.example.github.vo.Story
import java.io.IOException

/**
 * A task that uploads a created story to a remote database.
 */
class SaveStoryTask internal constructor(private val story: Story, private val githubService: GithubService) : Runnable {
    private val TAG = this.javaClass.simpleName
    private val isSuccessful = MutableLiveData<Boolean>()

    val liveData: LiveData<Boolean>
        get() = isSuccessful

    init {
        this.isSuccessful.postValue(false)
    }

    override fun run() {

        try {
            Log.i(TAG, "Trying to publish story: " + story)
            Log.i(TAG, story.toJson())
            val s = githubService.putStory(MainActivity.getCognitoToken(), story).execute()
            if (s.isSuccessful) {
                isSuccessful.postValue(true)
                Log.i(TAG, "Publish story success: " + s.body()!!)
            } else {
                isSuccessful.postValue(false)
                Log.i(TAG, "Publish story failed: " + s.body()!!)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
