package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.Story

class DeleteStoryTask(val story: Story, val db: WalkingTaleDb) : AbstractTask<Story, Story>(story, db) {

    override fun run() {
        val response = dynamoDBMapper.delete(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}