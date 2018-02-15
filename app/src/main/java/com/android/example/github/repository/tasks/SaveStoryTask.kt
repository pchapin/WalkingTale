package com.android.example.github.repository.tasks

import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.vo.Story

class SaveStoryTask(val story: Story) : AbstractTask<Story, Void>(story) {

    override fun run() {
        dynamoDBMapper.save(story)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}