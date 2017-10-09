package com.android.example.github

/**
 * 10/9/2017.
 */

class StoryManager() {
    fun visitedChapters() {

    }
}

class Story(chapters: List<Chapter>,
            title: String,
            description: String)

class Chapter(expositions: List<Exposition>,
              name: String)

class Exposition(type: ExpositionType,
                 content: String)

enum class ExpositionType {
    TEXT
}