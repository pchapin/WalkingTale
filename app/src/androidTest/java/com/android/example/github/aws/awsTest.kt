/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.github.aws

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.android.example.github.walkingTale.ExampleRepo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*

@RunWith(AndroidJUnit4::class)
class awsTest {
    private val S3_OBJECT_KEY = "ok"
    private val context = InstrumentationRegistry.getTargetContext()
    private val s3TransferUtility = Util.getTransferUtility(context)
    private val dynamoDbManager = DynamoDbManager(context)

    @Test
    @Throws(NotSerializableException::class)
    fun s3UploadTest() {
        val file = createTempFile()
        ObjectOutputStream(FileOutputStream(file!!)).use { oos ->
            oos.writeObject(ExampleRepo.getRepo())
            s3TransferUtility.upload(Constants.BUCKET_NAME, S3_OBJECT_KEY, file)
            //todo check if uploaded
        }
    }

    @Test
    fun s3DownloadTest() {
        val file = createTempFile()
        s3TransferUtility.download(Constants.BUCKET_NAME, S3_OBJECT_KEY, file!!)
        //todo check if downloaded
    }

    @Test
    fun ddbUploadTest() {

    }

    @Test
    fun ddbScanTest() {

    }

    @Test
    fun ddbUpdateTest() {

    }

    @Test
    fun ddbDeleteRepoTest() {

    }

    private fun createTempFile(): File? {
        val outputDir = context.cacheDir // context being the Activity pointer
        try {
            return File.createTempFile("prefix", "tmp", outputDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

}