package com.android.example.github.walkingTale

import com.android.example.github.MainActivity
import com.android.example.github.vo.Story
import java.util.*

/**
 * 11/15/17.
 */
class ExampleStory {
    companion object {
        fun getStory(): Story {
            return Story(
                    Random().nextDouble().toString(), "", "", mutableListOf<Chapter>(), "",
                    mutableListOf("tag1"), 10, 1.1, "", "", MainActivity.getCognitoId()
            )
        }

        fun getRandomStory(): Story {
            val repo = getStory()
            repo.id = Random().nextInt().toString()
            return repo
        }
    }
}