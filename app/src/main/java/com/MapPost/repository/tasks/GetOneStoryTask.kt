package com.MapPost.repository.tasks

import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.Story

class GetOneStoryTask(val storyKey: StoryKey, val db: WalkingTaleDb) : AbstractTask<StoryKey, Story>(storyKey, db) {

    override fun run() {
        val response = dynamoDBMapper.load(Story::class.java, storyKey.userId, storyKey.storyId)
        if (response != null) {
            result.postValue(Resource(Status.SUCCESS, response, null))
            db.beginTransaction()
            db.storyDao().insert(response)
            db.endTransaction()
        } else {
            result.postValue(Resource(Status.ERROR, null, null))
        }
    }
}

data class StoryKey(val userId: String, val storyId: String)