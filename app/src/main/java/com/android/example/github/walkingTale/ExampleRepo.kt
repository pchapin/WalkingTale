package com.android.example.github.walkingTale

import com.android.example.github.vo.Story
import java.util.*

/**
 * 11/15/17.
 */
class ExampleRepo {
    companion object {
        fun getRepo(): Story {
            return Story()
        }

        fun getRandomRepo(): Story {
            val repo = getRepo()
            repo.id = Random().nextInt().toString()
            return repo
        }
    }
}