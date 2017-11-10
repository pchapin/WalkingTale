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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(JUnit4.class)
public class awsTest {


    @Test
    public void testDynamoDb() {

        AWSCredentials awsCredentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "YOUR AWS ACCESS KEY HERE";
            }

            @Override
            public String getAWSSecretKey() {
                return "YOUR AWS SECRET KEY HERE";
            }
        };

        AmazonDynamoDBClient client = new AmazonDynamoDBClient(awsCredentials);
        Table myTable = Table.loadTable(client, "YOUR TABLE NAME HERE");

        Document myDocumentObject = Document.fromJson("{}");

//        String myJsonString = Document.toJson(myDocumentObject);

        myTable.putItem(myDocumentObject);

        assertThat("", 1 == 1);
    }

}