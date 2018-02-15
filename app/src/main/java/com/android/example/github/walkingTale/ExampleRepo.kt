package com.android.example.github.walkingTale

import com.android.example.github.MainActivity
import com.android.example.github.vo.Story
import java.util.*

/**
 * 11/15/17.
 */
class ExampleRepo {
    companion object {
        fun getRepo(): Story {
            return Story(
                    Random().nextDouble().toString(), "", "", mutableListOf<Chapter>(), "",
                    mutableListOf("tag1"), 10, 1.1, "", "", MainActivity.getCognitoId()
            )
        }

        fun getRandomRepo(): Story {
            val repo = getRepo()
            repo.id = Random().nextInt().toString()
            return repo
        }
    }
}