package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: GetAllCallback)
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }
}