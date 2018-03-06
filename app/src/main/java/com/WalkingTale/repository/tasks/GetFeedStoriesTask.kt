package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.Story
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