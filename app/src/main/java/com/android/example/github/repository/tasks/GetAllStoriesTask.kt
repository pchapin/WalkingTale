package com.android.example.github.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.Story

class GetAllStoriesTask(val nothing: String) : AbstractTask<String, MutableList<Story>>(nothing) {

    override fun run() {
        val response = dynamoDBMapper.scan(Story::class.java, DynamoDBScanExpression())
        result.postValue(Resource(Status.SUCCESS, response, null))
    }
}