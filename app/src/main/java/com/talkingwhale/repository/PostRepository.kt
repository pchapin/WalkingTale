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
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.google.android.gms.maps.model.LatLng
import com.talkingwhale.AppExecutors
import com.talkingwhale.R
import com.talkingwhale.repository.tasks.AbstractTask
import com.talkingwhale.vo.Post
import com.talkingwhale.vo.PostType
import com.talkingwhale.vo.Resource
import com.talkingwhale.vo.Status
import id.zelory.compressor.Compressor
import java.io.File
import java.lang.Exception


object PostRepository {

    private val tag = PostRepository.javaClass.simpleName
    private val appExecutors: AppExecutors = AppExecutors

    data class CornerLatLng(val northEast: LatLng, val southWest: LatLng)

    fun getNearbyPosts(cornerLatLng: CornerLatLng): LiveData<Resource<List<Post>>> {
        val result = object : AbstractTask<String, List<Post>>("") {
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
        val result = object : AbstractTask<Post, Unit>(post) {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.save(post), ""))
            }
        }
        appExecutors.networkIO().execute(result)
        return result.getResult()
    }

    fun putFile(pair: Pair<Post, Context>): LiveData<Resource<Post>> {
        val result = object : AbstractTask<Pair<Post, Context>, Post>(pair) {
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
                        Log.i(tag, "" + bytesCurrent)
                    }

                    override fun onStateChanged(id: Int, state: TransferState?) {
                        Log.i(tag, "" + state)
                    }

                    override fun onError(id: Int, ex: Exception?) {
                        Log.i(tag, "" + ex)
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
        val result = object : AbstractTask<Post, Unit>(post) {
            override fun run() {
                result.postValue(Resource(Status.SUCCESS, dynamoDBMapper.delete(post), ""))
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
}
