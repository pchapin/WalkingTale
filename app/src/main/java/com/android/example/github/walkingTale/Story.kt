package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/9/2017.
 */

class StoryCreateManager() {

    private val story = Story()
    private var expositionCount = 0

    fun addChapter(name: String, location: Location, radius: Int) {
        val chapter = Chapter(ArrayList(), name, location, story.chapters.size, radius)
        story.chapters.add(chapter)
    }

    fun addExposition(expositionType: ExpositionType, content: String) {
        val exposition = Exposition(expositionType, content, expositionCount++)
        story.chapters.last().expositions.add(exposition)
    }

    fun getAllChapters(): ArrayList<Chapter> {
        return story.chapters
    }

    fun removeChapter() {
        story.chapters.removeAt(story.chapters.size - 1)
    }

    fun incrementRadius() {
        story.chapters.last().radius++
    }

    fun decrementRadius() {
        story.chapters.last().radius--
    }
}

data class Story(var chapters: ArrayList<Chapter> = ArrayList(),
                 var title: String = "",
                 var description: String = "",
                 var id: Int = -1)

data class Chapter(var expositions: ArrayList<Exposition> = ArrayList(),
                   var name: String,
                   var location: Location,
                   var id: Int,
                   var radius: Int)

data class Exposition(var type: ExpositionType,
                      var content: String,
                      var id: Int)

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}