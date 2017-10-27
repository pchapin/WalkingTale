package com.android.example.github.walkingTale

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * 10/26/2017.
 */
class ExampleStory {
    private val storyManager = StoryCreateManager()
    private lateinit var story: Story

    private fun create() {
        var count = 0
        val lat = 1.2
        val long = 4.6
        storyManager.addChapter("name" + count++, LatLng(lat, long), 5)
        storyManager.addChapter("name" + count++, LatLng(lat, long), 5)
        storyManager.addChapter("name" + count++, LatLng(lat, long), 5)
        storyManager.addChapter("name" + count++, LatLng(lat, long), 5)
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