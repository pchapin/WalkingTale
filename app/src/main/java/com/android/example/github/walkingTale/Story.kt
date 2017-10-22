package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/9/2017.
 */

class StoryCreateManager() {

    private val story = Story()
    private var chapterCount = 0
    private var expositionCount = 0

    fun addChapter(name: String, location: Location, radius: Double) {
        val chapter = Chapter(ArrayList(), name, location, chapterCount++, radius)
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
}

data class Story(var chapters: ArrayList<Chapter> = ArrayList(),
                 var title: String = "",
                 var description: String = "",
                 var id: Int = -1)

data class Chapter(var expositions: ArrayList<Exposition> = ArrayList(),
                   var name: String,
                   var location: Location,
                   var id: Int,
                   var radius: Double)

data class Exposition(var type: ExpositionType,
                      var content: String,
                      var id: Int)

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}