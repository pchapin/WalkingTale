package com.talkingwhale.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.talkingwhale.pojos.Resource

abstract class AbstractTask<out INPUT, OUTPUT>(val input: INPUT) : Runnable {

    val tag: String = this.javaClass.simpleName
    val result = MutableLiveData<Resource<OUTPUT>>()
    private val dynamoDBClient: AmazonDynamoDBClient = Region.getRegion(Regions.US_EAST_1)
            .createClient(
                    AmazonDynamoDBClient::class.java,
                    AWSMobileClient.getInstance().credentialsProvider,
                    ClientConfiguration())
    val dynamoDBMapper: DynamoDBMapper

    fun getResult(): LiveData<Resource<OUTPUT>> {
        return result
    }

    init {
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).build()
    }

    override fun run() {

    }
}