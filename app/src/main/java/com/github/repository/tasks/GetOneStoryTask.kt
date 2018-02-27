package com.github.repository.tasks

import com.github.db.GithubDb
import com.github.vo.Resource
import com.github.vo.Status
import com.github.vo.Story

class GetOneStoryTask(val storyKey: StoryKey, val db: GithubDb) : AbstractTask<StoryKey, Story>(storyKey, db) {

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