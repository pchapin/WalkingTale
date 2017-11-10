package com.android.example.github.walkingTale

import com.android.example.github.vo.Repo
import com.google.android.gms.maps.model.LatLng

/**
 * 10/9/2017.
 */

class StoryCreateManager() {

    private val story = Story()
    private var expositionCount = 0
    private val minRadius = 1
    private val maxRadius = 10

    fun getStory(): Story {
        return story
    }

    fun addChapter(name: String, location: LatLng, radius: Int) {
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

    fun getLatestChapter(): Chapter {
        return story.chapters.last()
    }

    fun removeChapter() {
        story.chapters.removeAt(story.chapters.size - 1)
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    fun incrementRadius() {
        if (story.chapters.last().radius == maxRadius) {
            throw ArrayIndexOutOfBoundsException("Max radius size is already " + maxRadius)
        }
        story.chapters.last().radius++
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    fun decrementRadius() {
        if (story.chapters.last().radius == minRadius) {
            throw ArrayIndexOutOfBoundsException("Min radius size is already " + minRadius)
        }
        story.chapters.last().radius--
    }
}

class StoryPlayManager(repo: Repo) {

    private val story = Story()
    private var currentChapter: Chapter

    init {
        story.chapters = repo.chapters as ArrayList<Chapter>
        currentChapter = story.chapters[0]
    }


    fun getCurrentChapter(): Chapter {
        return currentChapter
    }

    fun getAllExpositions(): List<Exposition> {
        return story.chapters.map { it.expositions }.flatten()
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    fun goToNextChapter() {
        if (currentChapter == story.chapters.last()) {
            throw ArrayIndexOutOfBoundsException("Current chapter is already the last chapter.")
        }
        currentChapter = story.chapters[currentChapter.id + 1]
    }
}

data class Story(var chapters: ArrayList<Chapter> = ArrayList(),
                 var title: String = "",
                 var description: String = "",
                 var id: Int = -1)

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}