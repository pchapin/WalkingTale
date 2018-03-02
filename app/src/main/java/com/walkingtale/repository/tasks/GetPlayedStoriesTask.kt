package com.walkingtale.repository.tasks

import com.walkingtale.db.WalkingTaleDb
import com.walkingtale.vo.Resource
import com.walkingtale.vo.Status
import com.walkingtale.vo.Story
import com.walkingtale.vo.User

class GetPlayedStoriesTask(val user: User, val db: WalkingTaleDb) : AbstractTask<User, MutableList<Story>>(user, db) {

    override fun run() {
        val response = mutableListOf<Story>()

        user.playedStories.forEach {
            //TODO optimize, use a single query rather than multiple loads
            // Result will be null if story is not found
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