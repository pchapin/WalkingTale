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

package com.android.example.github.api

import android.arch.lifecycle.LiveData
import com.android.example.github.vo.Repo
import com.android.example.github.vo.User
import retrofit2.Call
import retrofit2.http.*

/**
 * REST API access points
 */
interface GithubService {
    @GET("stories/{id}")
    fun getRepo(@Path("id") id: String): LiveData<ApiResponse<Repo>>

    @GET("stories")
    fun getAllRepos(@Header("Authorization") authToken: String): LiveData<ApiResponse<List<Repo>>>

    @GET("stories")
    fun getAllReposTesting(@Header("Authorization") authToken: String): Call<List<Repo>>

    @PUT("stories")
    fun putStory(@Header("Authorization") authToken: String,
                 @Body repo: Repo): Call<Repo>

    @DELETE("stories/{id}")
    fun deleteStory(@Header("Authorization") authToken: String,
                    @Path("id") id: String): Call<Void>

    @GET("users")
    fun getAllUsers(@Header("Authorization") authToken: String): Call<List<User>>

    @PUT("users")
    fun putUser(@Header("Authorization") authToken: String,
                @Body user: User): Call<User>

    @DELETE("users/{id}")
    fun deleteUser(@Header("Authorization") authToken: String,
                   @Path("id") id: String): Call<Void>

    @GET("users/{id}")
    fun getUser(@Header("Authorization") authToken: String,
                @Path("id") id: String): LiveData<ApiResponse<User>>

    @GET("users/{id}")
    fun getUserTesting(@Header("Authorization") authToken: String,
                       @Path("id") id: String): Call<User>
}
