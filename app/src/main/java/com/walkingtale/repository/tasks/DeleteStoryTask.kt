package com.walkingtale.repository.tasks

import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story

class DeleteStoryTask(val story: Story, val db: WalkingTaleDb) : AbstractTask<Story, Story>(story, db) {

    override fun run() {
        val response = dynamoDBMapper.delete(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}