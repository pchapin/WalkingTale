package com.android.example.github.walkingTale

import com.android.example.github.vo.Repo

/**
 * 11/15/17.
 */
class ExampleRepo() {
    fun getRepo(): Repo {
        return Repo(
                1, "", "", "", Repo.Owner("", ""),
                1, mutableListOf(), "", "", "", "",
                "", 1.1, 1.1, ""
        )
    }
}