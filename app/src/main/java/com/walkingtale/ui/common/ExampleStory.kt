package com.walkingtale.ui.common

import com.walkingtale.MainActivity
import com.walkingtale.vo.Chapter
import com.walkingtale.vo.Story
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