package com.android.example.github.repository.tasks

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.s3.AmazonS3Client
import com.android.example.github.aws.s3BucketName
import com.android.example.github.db.GithubDb
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.Story

abstract class AbstractTask<out I, O>(val input: I,
                                      val database: GithubDb) : Runnable {

    val TAG = this.javaClass.simpleName
    val result = MutableLiveData<Resource<O>>()
    val dynamoDBClient: AmazonDynamoDBClient
    val dynamoDBMapper: DynamoDBMapper
    val transferUtilityBuilder: TransferUtility.Builder

    fun getResult(): LiveData<Resource<O>> {
        return result
    }

    init {
        dynamoDBClient = Region.getRegion(Regions.US_EAST_1)
                .createClient(
                        AmazonDynamoDBClient::class.java,
                        AWSMobileClient.getInstance().credentialsProvider,
                        ClientConfiguration())
        dynamoDBMapper = DynamoDBMapper(dynamoDBClient)
        transferUtilityBuilder = TransferUtility.builder()
                .defaultBucket(s3BucketName)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))

        result.postValue(Resource(Status.LOADING, null, null))
    }

    override fun run() {

    }
}

data class S3Args(val story: Story, val context: Context)