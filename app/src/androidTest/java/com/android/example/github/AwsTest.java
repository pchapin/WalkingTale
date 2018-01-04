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

package com.android.example.github;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.android.example.github.api.GithubService;
import com.android.example.github.api.RepoSearchResponse;
import com.android.example.github.util.LiveDataCallAdapterFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AwsTest {

    Context context = InstrumentationRegistry.getContext();
    String TAG = this.getClass().getSimpleName();
    String USER_ID = "todd";
    String PASSWORD = "Passw0rd!";
    String accessToken;

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            Log.i(TAG, String.format("success. Token:%s", userSession.getAccessToken().getJWTToken()));
            accessToken = userSession.getAccessToken().getJWTToken();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, PASSWORD, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            continuation.continueTask();
        }

        @Override
        public void onFailure(Exception exception) {
            Log.i(TAG, exception.toString());
        }
    };

    @Before
    public void init() {

    }

    @Test
    public void testListStories() throws IOException {
        // Get cognito accessToken
        CognitoUserPool userPool = new CognitoUserPool(context,
                "us-east-1_5Jew8wIVQ",
                "2aj12760enoi6kvs3bun08dsfg",
                "1tr05oa80sre89di3an5je81k5o78jq6thblkmcgrgo0mljb3li2");

        userPool.getUser(USER_ID).getSession(authenticationHandler);

        assertTrue(accessToken != null);

        // Make GET request to list endpoint
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://scfce9cwk7.execute-api.us-east-1.amazonaws.com/dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build();

        GithubService githubService = retrofit.create(GithubService.class);

        Response<RepoSearchResponse> response = null;
        response = githubService.getAllRepos().execute();

        assertNotNull(response.body());
        assertNotNull(response.body().getItems());
    }
}