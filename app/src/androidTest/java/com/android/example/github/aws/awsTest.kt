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
import android.util.Log
import android.webkit.DownloadListener
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.android.example.github.repository.Constants
import com.android.example.github.repository.Util
import com.android.example.github.vo.Repo
import com.android.example.github.walkingTale.Chapter
import com.android.example.github.walkingTale.ExampleRepo
import com.google.android.gms.maps.model.LatLng
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*
import java.nio.file.Files

@RunWith(AndroidJUnit4::class)
class awsTest {
    private val S3_OBJECT_KEY = "ok"
    private val context = InstrumentationRegistry.getTargetContext()
    private val s3TransferUtility = Util.getTransferUtility(context)
    private val ddbClient = AmazonDynamoDBClient(CognitoCachingCredentialsProvider(context, Constants.COGNITO_POOL_ID, Regions.US_EAST_1))
    private val dynamoDBMapper = DynamoDBMapper(ddbClient)

    @Test
    @Throws(NotSerializableException::class)
    fun s3UploadTest() {
        val file = createTempFile()
        try {
            ObjectOutputStream(FileOutputStream(file!!)).use { oos ->
                oos.writeObject(ExampleRepo.getRepo())
                val observer = s3TransferUtility.upload(Constants.BUCKET_NAME, S3_OBJECT_KEY, file)

                observer.setTransferListener(object : TransferListener {
                    override fun onStateChanged(id: Int, state: TransferState) {

                    }

                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        // todo: why is this method not being called?
                        Log.i("s3", "progress changed " + bytesCurrent + "/" + bytesTotal)
                    }

                    override fun onError(id: Int, ex: Exception) {

                    }
                })
            }
        } catch (e: IOException) {
            println(e)
        }
    }

    @Test
    fun s3DownloadTest() {
        val file = createTempFile()
        s3TransferUtility.download(Constants.BUCKET_NAME, S3_OBJECT_KEY, file!!).setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {

            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                if (bytesCurrent == bytesTotal) {
                    try {
                        val contents = String(Files.readAllBytes(file.toPath()))
                        val repo = Repo.fromString(contents)

                        Log.i("s3", "download result" + repo.toString())

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onError(id: Int, ex: Exception) {

            }
        })
    }

    @Test
    fun ddbUploadTest() {
        val repo = ExampleRepo.getRepo()
        val repo2 = scanDbb().get(0)
        dynamoDBMapper.save(repo)
        Log.i("ddb", "local repo" + repo + repo.javaClass)
        Log.i("ddb", "remote repo" + repo2 + repo2.javaClass)
        Log.i("ddb", "" + (repo == repo2))
        //todo: why are these not "equal" even though they have the same values?
        assertTrue(scanDbb().contains(repo))
    }

    @Test
    fun ddbScanTest() {
        val repos = scanDbb()
        for (repo in repos) {
            Log.i("ddb", "scan result " + repo.toString())
        }
        assertTrue(!repos.isEmpty())
    }

    @Test
    fun ddbUpdateTest() {
    }

    @Test
    fun ddbDeleteRepoTest() {
    }


    /**
     * @return A list of all the repos in the ddb Repo table
     */
    fun scanDbb(): PaginatedScanList<Repo> {
        val dynamoDBQueryExpression = DynamoDBScanExpression()
        dynamoDBMapper.scan(Repo::class.java, dynamoDBQueryExpression)
        return dynamoDBMapper.scan(Repo::class.java, dynamoDBQueryExpression)
    }

    fun createTempFile(): File? {
        val outputDir = context.cacheDir // context being the Activity pointer
        try {
            return File.createTempFile("prefix", "tmp", outputDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

}