package com.talkingwhale.pojos

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.talkingwhale.db.AppDatabase
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostTest {

    val tag = PostTest::class.java.simpleName
    lateinit var db: AppDatabase
    lateinit var post: Post

    @Before
    fun setup() {
        db = AppDatabase.getInMemoryDatabase(InstrumentationRegistry.getTargetContext())
        post = Post()
    }

    @After
    fun after() {
        db.close()
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

    @Test
    fun multipleTest() {
        val posts = mutableListOf(Post(), Post(), Post())
        db.postDao().insertPosts(posts)
        val result = db.postDao().loadPosts(posts.map { it.postId })
        assertEquals(result.size, 3)
    }
}