package com.android.example.github.repository.tasks

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.android.example.github.db.GithubDb
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.Story

class GetOneStoryTask(private val storyId: String, val db: GithubDb) : AbstractTask<String, Story>(storyId, db) {

    override fun run() {
        val response = dynamoDBMapper.scan(Story::class.java, DynamoDBScanExpression())
        val oneStory = response.filter { it.id == storyId }
        if (oneStory.isEmpty()) {
            result.postValue(Resource(Status.ERROR, null, null))
        } else {
            result.postValue(Resource(Status.SUCCESS, response[0], null))
            db.beginTransaction()
            db.repoDao().insert(response[0])
            db.endTransaction()
        }
    }
}