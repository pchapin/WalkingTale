package com.android.example.github.repository.tasks

import com.android.example.github.db.GithubDb
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.Story

class SaveStoryTask(val story: Story, db: GithubDb) : AbstractTask<Story, Void>(story, db) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}