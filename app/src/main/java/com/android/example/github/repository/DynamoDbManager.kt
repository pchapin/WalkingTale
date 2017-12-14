package com.android.example.github.repository

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.android.example.github.vo.Repo

class DynamoDbManager(context: Context) {

    private val ddbClient = AmazonDynamoDBClient(CognitoCachingCredentialsProvider(context, Constants.COGNITO_POOL_ID, Regions.US_EAST_1))
    private val dynamoDBMapper = DynamoDBMapper(ddbClient)

    fun ddbUploadRepo(repo: Repo) {
        dynamoDBMapper.save(repo)
    }

    fun ddbDeleteRepo(repo: Repo) {
        dynamoDBMapper.delete(repo)
    }

    fun scanDbb(): PaginatedScanList<Repo> {
        val dynamoDBQueryExpression = DynamoDBScanExpression()
        dynamoDBMapper.scan(Repo::class.java, dynamoDBQueryExpression)
        return dynamoDBMapper.scan(Repo::class.java, dynamoDBQueryExpression)
    }

    fun updateRepo(repo: Repo) {
        if (!scanDbb().contains(repo))
            throw(IllegalArgumentException("Error: ddb must contain repo before updating."))
        dynamoDBMapper.save(repo, DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES))
    }

}
