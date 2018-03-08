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

package com.MapPost.db;

import android.arch.persistence.room.TypeConverter;

import com.MapPost.vo.Chapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class WalkingTaleTypeConverters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static List<Chapter> stringToChapterList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Chapter>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String chapterListToString(List<Chapter> chapters) {
        return gson.toJson(chapters);
    }

    @TypeConverter
    public static String listOfStringToString(List<String> stringList) {
        return gson.toJson(stringList);
    }

    @TypeConverter
    public static List<String> stringToListOfString(String jsonString) {
        return gson.fromJson(jsonString, new TypeToken<List<String>>() {
        }.getType());
    }
}
