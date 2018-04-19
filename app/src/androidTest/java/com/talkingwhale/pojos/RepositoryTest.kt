package com.talkingwhale.pojos

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.talkingwhale.repository.PostGroupRepository
import com.talkingwhale.repository.PostRepository
import com.talkingwhale.repository.UserRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {

    val tag = RepositoryTest::class.java.simpleName
    val context = InstrumentationRegistry.getTargetContext()
    val userRepository = UserRepository
    val postRepository = PostRepository
    val postGroupRepository = PostGroupRepository
    val user = User()

    @Before
    fun setup() {
    }

    @Test
    fun createUserTest() {
        userRepository.putUser(user)
    }

    @Test
    fun createPostTest() {
        postRepository.addPost(Post())
    }

    @Test
    fun deleteUserTest() {
        userRepository.deleteUser(user)
    }
}