package com.WalkingTale.ui.common

import com.WalkingTale.MainActivity
import com.WalkingTale.vo.Chapter
import com.WalkingTale.vo.Story
import java.util.*

/**
 * 11/15/17.
 */
class ExampleStory {
    companion object {
        fun getStory(): Story {
            return Story(
                    Random().nextLong().toString(), "", "", mutableListOf<Chapter>(), "",
                    mutableListOf("tag1"), 10, 1.1, "", "", MainActivity.getCognitoId()
            )
        }
    }
}