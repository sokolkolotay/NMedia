package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: PostCallback<List<Post>>)
    fun getAll(): List<Post>
    fun likeById(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun save(post: Post, callback: PostCallback<Post>)
    fun removeById(id: Long, callback: PostCallback<Unit>)


    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Throwable)
    }
}
