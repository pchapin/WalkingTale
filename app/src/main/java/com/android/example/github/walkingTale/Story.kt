package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/9/2017.
 */

class StoryManager() {

    private val story = Story()

    fun addChapter(chapter: Chapter) {
        story.chapters.add(chapter)
    }

    fun addExposition(exposition: Exposition) {
        story.chapters.last().expositions.add(exposition)
    }

    fun addExposition(exposition: Exposition, chapter: Chapter) {
        val index = story.chapters.indexOf(chapter)
        story.chapters[index].expositions.add(exposition)
    }

    fun removeChapter(chapter: Chapter) {
        story.chapters.remove(chapter)
    }

    fun removeExposition(exposition: Exposition) {
        story.chapters.last().expositions.remove(exposition)
    }

    fun getChapter(nameOfChapter: String) {

    }

    fun getExposition() {

    }
}

data class Story(var chapters: MutableList<Chapter> = mutableListOf(),
                 var title: String = "",
                 var description: String = "")

data class Chapter(var expositions: MutableList<Exposition>,
                   var name: String,
                   var location: Location)

data class Exposition(var type: ExpositionType,
                      var content: String)

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}