package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.Story

class SaveStoryTask(val story: Story, db: WalkingTaleDb) : AbstractTask<Story, Void>(story, db) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}