package com.MapPost.repository.tasks

import com.MapPost.db.WalkingTaleDb
import com.MapPost.vo.Resource
import com.MapPost.vo.Status
import com.MapPost.vo.Story

class SaveStoryTask(val story: Story, db: WalkingTaleDb) : AbstractTask<Story, Void>(story, db) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}