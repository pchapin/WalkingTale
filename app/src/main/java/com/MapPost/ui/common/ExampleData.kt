package com.MapPost.ui.common

import com.MapPost.MainActivity
import com.MapPost.vo.Chapter
import com.MapPost.vo.Story
import com.MapPost.vo.User
import java.util.*

/**
 * 11/15/17.
 */
class ExampleData {
    companion object {
        fun getStory(): Story {
            return Story(
                    Random().nextLong().toString(), "", "", mutableListOf<Chapter>(), "",
                    mutableListOf("tag1"), 10, 1.1, "", "", MainActivity.getCognitoId()
            )
        }

        fun getUser(): User {
            return User()
        }
    }
}