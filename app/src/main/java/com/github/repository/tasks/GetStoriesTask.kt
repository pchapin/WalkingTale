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

package com.github.repository.tasks

import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.github.api.GithubService
import com.github.vo.Story


/**
 * A task that uploads a created story to a remote database.
 */
class GetStoriesTask internal constructor(private val githubService: GithubService) : Runnable {
    private val TAG = this.javaClass.simpleName

    override fun run() {

        val dynamoDBClient = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
        val dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .build()

        val story: Story = dynamoDBMapper.load(Story::class.java, "12345")

        Log.i(TAG, "$story")
    }
}
