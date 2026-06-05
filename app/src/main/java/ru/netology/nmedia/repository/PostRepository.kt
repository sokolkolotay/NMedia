package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: GetAllCallback)
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long)
    suspend fun getNewer(id: Long): Int  // возвращает количество новых постов
    suspend fun showNewPosts()           // делает скрытые посты видимыми

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }
}