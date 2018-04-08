package com.talkingwhale.pojos

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "mappost-mobilehub-452475001-PostGroup")
class PostGroup(
        @get:DynamoDBHashKey(attributeName = "userId")
        @get:DynamoDBAttribute(attributeName = "userId")
        var userId: String,
        @get:DynamoDBRangeKey(attributeName = "id")
        @get:DynamoDBAttribute(attributeName = "id")
        var id: String,
        @get:DynamoDBAttribute(attributeName = "postIdList")
        var postIdList: MutableList<String>
)