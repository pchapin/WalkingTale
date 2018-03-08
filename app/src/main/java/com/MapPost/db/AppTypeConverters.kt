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

package com.MapPost.db

import android.arch.persistence.room.TypeConverter
import com.MapPost.vo.Chapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun stringToChapterList(data: String?): List<Chapter> {
        if (data == null) {
            return emptyList()
        }

        val listType = object : TypeToken<List<Chapter>>() {

        }.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun chapterListToString(chapters: List<Chapter>): String {
        return gson.toJson(chapters)
    }

    @TypeConverter
    fun listOfStringToString(stringList: List<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun stringToListOfString(jsonString: String): List<String> {
        return gson.fromJson(jsonString, object : TypeToken<List<String>>() {

        }.type)
    }
}
