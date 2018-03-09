package com.MapPost.repository.tasks

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.MapPost.vo.Post
import com.MapPost.vo.Resource
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient

abstract class AbstractTask<out INPUT, OUTPUT>(val input: INPUT) : Runnable {

    val TAG = this.javaClass.simpleName
    val result = MutableLiveData<Resource<OUTPUT>>()
    val dynamoDBClient: AmazonDynamoDBClient
    val dynamoDBMapper: DynamoDBMapper

    fun getResult(): LiveData<Resource<OUTPUT>> {
        return result
    }

    init {
        dynamoDBClient = Region.getRegion(Regions.US_EAST_1)
                .createClient(
                        AmazonDynamoDBClient::class.java,
                        AWSMobileClient.getInstance().credentialsProvider,
                        ClientConfiguration())
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).build()
    }

    override fun run() {

    }
}

data class S3Args(val post: Post, val context: Context)