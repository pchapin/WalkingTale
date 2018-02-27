package com.walkingtale.repository.tasks

import com.walkingtale.db.GithubDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story

class SaveStoryTask(val story: Story, db: GithubDb) : AbstractTask<Story, Void>(story, db) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}