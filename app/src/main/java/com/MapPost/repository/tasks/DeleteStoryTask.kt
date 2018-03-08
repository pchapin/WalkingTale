package com.MapPost.repository.tasks

import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.Story

class DeleteStoryTask(val story: Story, val db: WalkingTaleDb) : AbstractTask<Story, Story>(story, db) {

    override fun run() {
        val response = dynamoDBMapper.delete(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}