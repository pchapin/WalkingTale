package com.MapPost.repository.tasks

import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.Story
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression

class GetFeedStoriesTask(val nothing: String, val db: WalkingTaleDb) : AbstractTask<String, MutableList<Story>>(nothing, db) {

    override fun run() {
        val response = dynamoDBMapper.scan(Story::class.java, DynamoDBScanExpression())
        db.beginTransaction()
        response.forEach {
            db.storyDao().insert(it)
        }
        db.endTransaction()
        result.postValue(Resource(Status.SUCCESS, response, null))
    }
}