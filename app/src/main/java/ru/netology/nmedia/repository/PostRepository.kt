package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post>                          //старая реализация
//    fun likeById(id: Long, likedByMe: Boolean): Post  //старая реализация
//    fun save(post: Post)                              //старая реализация
//    fun removeById(id: Long)                          //старая реализация
    fun getAllAsync(callback: GetAllCallback)                                //новая реализация через OkHttp
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: LikeByIdCallback) //новая реализация через OkHttp
    fun saveAsync(post: Post, callback: SaveCallback)                           //новая реализация через OkHttp
    fun removeByIdAsync(id: Long, callback: RemoveByIdCallback)                 //новая реализация через OkHttp
    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Exception)
    }

    interface LikeByIdCallback {
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }

    interface SaveCallback {
        fun onSuccess(post: Post)
        fun onError(e: Exception)
    }

    interface RemoveByIdCallback {
        fun onSuccess()
        fun onError(e: Exception)
    }
}
