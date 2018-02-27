package com.walkingtale.repository.tasks

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story

abstract class AbstractTask<out I, O>(val input: I,
                                      val database: WalkingTaleDb) : Runnable {

    val TAG = this.javaClass.simpleName
    val result = MutableLiveData<Resource<O>>()
    val dynamoDBClient: AmazonDynamoDBClient
    val dynamoDBMapper: DynamoDBMapper

    fun getResult(): LiveData<Resource<O>> {
        return result
    }

    init {
        dynamoDBClient = Region.getRegion(Regions.US_EAST_1)
                .createClient(
                        AmazonDynamoDBClient::class.java,
                        AWSMobileClient.getInstance().credentialsProvider,
                        ClientConfiguration())
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).build()

        result.postValue(Resource(Status.LOADING, null, null))
    }

    override fun run() {

    }
}

data class S3Args(val story: Story, val context: Context)