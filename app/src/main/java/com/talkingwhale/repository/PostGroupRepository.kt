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

package com.talkingwhale.repository

import android.arch.lifecycle.LiveData
import com.talkingwhale.pojos.PostGroup
import com.talkingwhale.pojos.Resource
import com.talkingwhale.pojos.Status
import com.talkingwhale.util.AppExecutors

/**
 */
object PostGroupRepository {
    private val appExecutors: AppExecutors = AppExecutors

    fun putPostGroup(postGroup: PostGroup): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(postGroup), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }
}
