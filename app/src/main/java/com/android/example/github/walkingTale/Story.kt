package com.android.example.github.walkingTale

import android.location.Location

/**
 * 10/9/2017.
 */

class StoryManager() {

    private val story = Story()
    lateinit var currentChapter: Chapter;

    fun getNextChapter(): Chapter {
        return story.chapters.get(currentChapter.id + 1)
    }

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

data class Story(var chapters: ArrayList<Chapter> = ArrayList(),
                 var title: String = "",
                 var description: String = "",
                 var id: Int = -1,
                 var chapterCount: Int = -1)

data class Chapter(var expositions: ArrayList<Exposition> = ArrayList(),
                   var name: String,
                   var location: Location,
                   var visited: Boolean = false,
                   var id: Int = -1,
                   var expositionCount: Int = -1)

data class Exposition(var type: ExpositionType,
                      var content: String,
                      var viewed: Boolean = false,
                      var id: Int = -1)

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}