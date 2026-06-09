package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long)
    suspend fun getNewer(id: Long): Int
    suspend fun showNewPosts()
}