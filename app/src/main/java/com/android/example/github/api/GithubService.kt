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
    @GET("users/{login}")
    fun getUser(@Path("login") login: String): LiveData<ApiResponse<User>>

    @GET("stories/{id}")
    fun getRepo(@Path("id") id: String): LiveData<ApiResponse<Repo>>

    @GET("stories")
    fun searchRepos(): LiveData<ApiResponse<RepoSearchResponse>>

    @GET("toddcooke/test/master/test.json")
    fun searchRepos(@Query("q") query: String, @Query("page") page: Int): Call<RepoSearchResponse>

    @GET("stories")
    fun getAllRepos(@Header("Authorization") authToken: String): Call<RepoSearchResponse>

    @PUT("stories")
    fun putStory(@Header("Authorization") authToken: String,
                 @Body repo: Repo): Call<Repo>
}
