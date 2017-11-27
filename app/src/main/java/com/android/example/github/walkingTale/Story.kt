package com.android.example.github.walkingTale

import com.android.example.github.vo.Repo
import com.google.android.gms.maps.model.LatLng

/**
 * 10/9/2017.
 */

class StoryPlayManager(repo: Repo) {

    private var story = repo
    private var currentChapter = story.chapters[0]

    fun getCurrentChapter(): Chapter = currentChapter

    @Throws(ArrayIndexOutOfBoundsException::class)
    fun goToNextChapter() {
        if (currentChapter == story.chapters.last()) {
            throw ArrayIndexOutOfBoundsException("Current chapter is already the last chapter.")
        }
        currentChapter = story.chapters[currentChapter.id + 1]
    }
}

enum class ExpositionType {
    TEXT, AUDIO, PICTURE
}