package com.talkingwhale.vo

import android.support.test.InstrumentationRegistry
import com.talkingwhale.db.AppDatabase
import org.junit.Before
import org.junit.Test

class PostTest {

    lateinit var db: AppDatabase
    lateinit var post: Post

    @Before
    fun setup() {
        db = AppDatabase.getInMemoryDatabase(InstrumentationRegistry.getContext())
        post = Post()
    }

    @Test
    fun insertTest() {
        db.postDao().insert(post)
    }

    @Test
    fun loadTest() {
        db.postDao().load(post.postId)
    }

    @Test
    fun deleteTest() {
        db.postDao().delete(post)
    }
}