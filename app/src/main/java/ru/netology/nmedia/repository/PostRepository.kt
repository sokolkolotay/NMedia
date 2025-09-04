package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post> //старая реализация
//    fun likeById(id: Long, likedByMe: Boolean): Post
    fun save(post: Post)
    fun removeById(id: Long)
    fun getAllAsync(callback: GetAllCallback) //новая реализация через OkHttp
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: LikeByIdAsync) //новая реализация через OkHttp

    interface GetAllCallback { //для новой реализации через OkHttp
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }

    interface LikeByIdAsync {
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }
}
