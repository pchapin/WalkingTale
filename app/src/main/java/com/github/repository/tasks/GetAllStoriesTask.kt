package com.github.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.github.db.GithubDb
import com.github.vo.Resource
import com.github.vo.Status
import com.github.vo.Story

class GetAllStoriesTask(val nothing: String, val db: GithubDb) : AbstractTask<String, MutableList<Story>>(nothing, db) {

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