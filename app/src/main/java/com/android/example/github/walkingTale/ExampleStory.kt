package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/26/2017.
 */
class ExampleStory {
    private val storyManager = StoryCreateManager()
    private lateinit var story: Story

    private fun create() {
        var count = 0
        storyManager.addChapter("name" + count++, Location(""), 5)
        storyManager.addChapter("name" + count++, Location(""), 5)
        storyManager.addChapter("name" + count++, Location(""), 5)
        storyManager.addChapter("name" + count++, Location(""), 5)
        storyManager.addExposition(ExpositionType.TEXT, "HELLO")
        story = storyManager.getStory()
    }

    fun getStory(): Story {
        return story
    }

    init {
        create()
    }
}