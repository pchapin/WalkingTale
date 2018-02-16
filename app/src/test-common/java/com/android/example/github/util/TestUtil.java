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

package com.android.example.github.util;

import com.android.example.github.vo.Story;
import com.android.example.github.vo.User;
import com.android.example.github.walkingTale.ExampleStory;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static User createUser(String login) {
        return null;
    }

    public static List<Story> createRepos(int count, String owner, String name,
                                          String description) {
        List<Story> stories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            stories.add(createRepo(owner + i, name + i, description + i));
        }
        return stories;
    }

    public static Story createRepo(String owner, String name, String description) {
        return createRepo(Story.UNKNOWN_ID, owner, name, description);
    }

    public static Story createRepo(int id, String owner, String name, String description) {
        return ExampleStory.Companion.getStory();
    }
}
