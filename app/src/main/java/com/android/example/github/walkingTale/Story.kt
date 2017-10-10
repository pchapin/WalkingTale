package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/9/2017.
 */

class StoryManager() {

    val story = Story()

    fun addChapter() {

    }

    fun removeChapter() {}
    fun addExposition() {}
    fun removeExposition() {}
}

class Story(val chapters: MutableList<Chapter> = mutableListOf(),
            val title: String = "",
            val description: String = "")

class Chapter(expositions: List<Exposition>,
              name: String,
              location: Location)

class Exposition(type: ExpositionType,
                 content: String)

enum class ExpositionType {
    TEXT
}