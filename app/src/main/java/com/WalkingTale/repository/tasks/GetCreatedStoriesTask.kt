package com.WalkingTale.repository.tasks

import com.WalkingTale.db.WalkingTaleDb
import com.WalkingTale.vo.Resource
import com.WalkingTale.vo.Status
import com.WalkingTale.vo.Story
import com.WalkingTale.vo.User

class GetCreatedStoriesTask(val user: User, val db: WalkingTaleDb) : AbstractTask<User, MutableList<Story>>(user, db) {

    override fun run() {
        val response = mutableListOf<Story>()

        user.createdStories.forEach {
            //TODO optimize, use a single query rather than multiple loads
            // Result will be null if story not found
            val result = dynamoDBMapper.load(Story::class.java, user.userId, it)
            if (result != null) {
                response.add(result)
            }
        }

        db.beginTransaction()
        response.forEach {
            db.storyDao().insert(it)
        }
        db.endTransaction()

        result.postValue(Resource(Status.SUCCESS, response, null))
    }
}