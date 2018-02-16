package com.android.example.github.repository.tasks

import com.android.example.github.db.GithubDb
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import com.android.example.github.walkingTale.ExpositionType
import java.io.File

/**
 * Uploads a file to S3
 * */
class PutFileS3Task(private val s3Args: S3Args, val db: GithubDb) :
        AbstractTask<S3Args, String>(s3Args, db) {

    override fun run() {
        val transferUtility = transferUtilityBuilder.context(s3Args.context).build()

        val expositions = s3Args.story.chapters
                .flatMap { it.expositions }
                .filter { it.type == ExpositionType.AUDIO || it.type == ExpositionType.PICTURE }

        expositions.forEach {
            transferUtility.upload("${s3Args.story.id}/${it.id}", File(it.content))
        }
        result.postValue(Resource(Status.SUCCESS, null, null))
    }
}