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

import com.android.example.github.repository.Constants;
import com.android.example.github.repository.Util;
import com.android.example.github.walkingTale.ExampleRepo;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@RunWith(AndroidJUnit4.class)
public class awsTest {
    private final String S3_OBJECT_KEY = "ok";
    private Context context = InstrumentationRegistry.getTargetContext();

    @Test
    public void s3UploadTest() {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File file = null;
        try {
            file = File.createTempFile("prefix", "extension", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(ExampleRepo.Companion.getRepo().toString());
            Util.getS3Client(context).putObject(Constants.BUCKET_NAME, S3_OBJECT_KEY, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void s3DownloadTest() {
        Util.getS3Client(context).getObject(Constants.BUCKET_NAME, S3_OBJECT_KEY);
    }

    @Test
    public void ddbUploadTest() {
    }

    @Test
    public void ddbDownloadTest() {
    }
}