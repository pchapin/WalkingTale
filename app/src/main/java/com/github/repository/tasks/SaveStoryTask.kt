package com.github.repository.tasks

import com.github.db.GithubDb
import com.github.vo.Resource
import com.github.vo.Status
import com.github.vo.Story

class SaveStoryTask(val story: Story, db: GithubDb) : AbstractTask<Story, Void>(story, db) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}