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

package com.android.example.github.aws;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.regions.Regions;
import com.android.example.github.repository.Constants;
import com.android.example.github.repository.Util;
import com.android.example.github.vo.Repo;
import com.android.example.github.walkingTale.ExampleRepo;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.isNotNull;

@RunWith(AndroidJUnit4.class)
public class awsTest {
    private final String S3_OBJECT_KEY = "ok";
    private Context context = InstrumentationRegistry.getTargetContext();
    private TransferUtility transferUtility = Util.getTransferUtility(context);
    private AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(new CognitoCachingCredentialsProvider(context, Constants.COGNITO_POOL_ID, Regions.US_EAST_1));
    private DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

    @Test
    public void s3UploadTest() throws NotSerializableException {
        File file = createTempFile();
        Log.i("starting s3 upload", "now");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(ExampleRepo.Companion.getRepo().toString());
            transferUtility.upload(Constants.BUCKET_NAME, "key", file).setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {

                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    if (bytesCurrent == bytesTotal) {
                        Log.i("starting s3 upload complete", "");
                    }
                }

                @Override
                public void onError(int id, Exception ex) {

                }
            });

        } catch (NotSerializableException e) {
            throw e;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void s3DownloadTest() {
        File file = createTempFile();
        Log.i("starting s3 download", "");
        transferUtility.download(Constants.BUCKET_NAME, S3_OBJECT_KEY, file).setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (bytesCurrent == bytesTotal) {
                    Log.i("s3 download result", "" + file);
                    try {
                        String contents = new String(Files.readAllBytes(file.toPath()));
                        Repo repo = Repo.fromString(contents);

                        Log.i("s3 downloaded", repo.toString());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    @Test
    public void ddbUploadTest() {
        mapper.save(ExampleRepo.Companion.getRepo());
    }

    @Test
    public void ddbDownloadTest() {
    }

    public File createTempFile() {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        try {
            return File.createTempFile("prefix", "tmp", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}