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
import com.MapPost.repository.tasks.AbstractTask
import com.MapPost.vo.Post
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression


object PostRepository {

    private val TAG = this.javaClass.simpleName
    private val appExecutors: AppExecutors = AppExecutors()

    fun getNearbyPosts(): LiveData<Resource<List<Post>>> {
        val result = object : AbstractTask<String, List<Post>>("") {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.scan(Post::class.java, DynamoDBScanExpression()), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun addPost(post: Post): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Post, Unit>(post) {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

}
