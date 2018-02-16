package com.android.example.github.repository.tasks

import com.android.example.github.db.GithubDb
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import java.io.File

/**
 * Uploads a file and returns the cloudfront location as a string
 * */
class PutFileS3Task(private val fileToUpload: Pair<String, File>, val db: GithubDb) : AbstractTask<Pair<String, File>, String>(fileToUpload, db) {

    override fun run() {
        val response = transferUtility.upload(fileToUpload.first, fileToUpload.second)
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}