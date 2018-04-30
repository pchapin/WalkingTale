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
import android.content.Context
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.MultiObjectDeleteException
import com.google.android.gms.maps.model.LatLng
import com.talkingwhale.R
import com.talkingwhale.pojos.*
import com.talkingwhale.util.AppExecutors
import id.zelory.compressor.Compressor
import java.io.File
import java.lang.Exception


object PostRepository {

    private val appExecutors: AppExecutors = AppExecutors

    data class CornerLatLng(val northEast: LatLng, val southWest: LatLng)

    fun getNearbyPosts(cornerLatLng: CornerLatLng): LiveData<Resource<List<Post>>> {
        val result = object : AbstractTask<List<Post>>() {
            override fun run() {

                val expressionAttributeValues = HashMap<String, AttributeValue>()
                expressionAttributeValues[":neLat"] = AttributeValue().withN(cornerLatLng.northEast.latitude.toString())
                expressionAttributeValues[":neLong"] = AttributeValue().withN(cornerLatLng.northEast.longitude.toString())
                expressionAttributeValues[":swLat"] = AttributeValue().withN(cornerLatLng.southWest.latitude.toString())
                expressionAttributeValues[":swLong"] = AttributeValue().withN(cornerLatLng.southWest.longitude.toString())

                val scanExpression = DynamoDBScanExpression()
                        .withFilterExpression("latitude <= :neLat and longitude <= :neLong and latitude >= :swLat and longitude >= :swLong")
                        .withExpressionAttributeValues(expressionAttributeValues)
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.scan(Post::class.java, scanExpression), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun addPost(post: Post): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun putFile(pair: Pair<Post, Context>): LiveData<Resource<Post>> {
        val result = object : AbstractTask<Post>() {
            override fun run() {
                val post = pair.first
                var file = File(pair.first.content)
                if (post.type == PostType.PICTURE) {
                    file = Compressor(pair.second).compressToFile(file)
                }
                val transferUtility = getTransferUtility(pair.second)
                val s3Path = post.postId
                transferUtility.upload(s3Path, file).setTransferListener(object : TransferListener {
                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {
                    }

                    override fun onError(id: Int, ex: Exception?) {
                    }
                })
                post.content = s3Path
                result.postValue(Resource(Status.SUCCESS, post, null))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun deletePost(post: Post): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.delete(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun putPosts(posts: List<Post>): LiveData<Resource<List<DynamoDBMapper.FailedBatch>>> {
        val result = object : AbstractTask<List<DynamoDBMapper.FailedBatch>>() {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.batchSave(posts), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun getPostsForUser(userId: String): LiveData<Resource<List<Post>>> {
        val result = object : AbstractTask<List<Post>>() {
            override fun run() {
                val queryExpression = DynamoDBQueryExpression<Post>()
                        .withHashKeyValues(Post().copy(userId = userId))
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.query(Post::class.java, queryExpression), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun getTransferUtility(context: Context): TransferUtility {
        val amazonS3 = AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider)
        amazonS3.setS3ClientOptions(S3ClientOptions.builder().disableChunkedEncoding().build())
        return TransferUtility.builder()
                .defaultBucket(context.resources.getString(R.string.s3_bucket))
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(amazonS3)
                .context(context)
                .build()
    }

    fun deleteUserS3Content(context: Context, user: User): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                if (user.createdPosts.isEmpty()) {
                    result.postValue(Resource(Status.SUCCESS, Unit, ""))
                    return
                }

                val s3Client = AmazonS3Client(IdentityManager.getDefaultIdentityManager().credentialsProvider.credentials)
                val deleteObjectsRequest = DeleteObjectsRequest(context.resources.getString(R.string.s3_bucket))
                        .withKeys(*user.createdPosts.toTypedArray())

                try {
                    s3Client.deleteObjects(deleteObjectsRequest)
                    result.postValue(Resource(Status.SUCCESS, Unit, ""))
                } catch (e: MultiObjectDeleteException) {
                    result.postValue(Resource(Status.ERROR, Unit, ""))
                }
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun deleteUsersPosts(user: User, posts: List<Post>): LiveData<Resource<Unit>> {
        val result = object : AbstractTask<Unit>() {
            override fun run() {
                val failed = dynamoDBMapper.batchDelete(posts)
                result.postValue(Resource(if (failed.isEmpty()) Status.SUCCESS else Status.ERROR, Unit, ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }
}
