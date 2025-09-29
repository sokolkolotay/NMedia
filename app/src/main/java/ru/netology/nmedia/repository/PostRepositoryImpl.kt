package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl : PostRepository {

    override fun getAll(): List<Post> {
        return PostApi.service.getAll()
            .execute()
            .let {
                if (it.isSuccessful) {
                    it.body() ?: throw RuntimeException("body is null")
                } else {
                    throw RuntimeException(it.message())
                }
            }
    }

    override fun getAllAsync(callback: PostRepository.PostCallback<List<Post>>) {
        PostApi.service.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (!response.isSuccessful) {
                    when (response.code()) {
                        404 -> callback.onError(RuntimeException("Post not found"))
                        500 -> callback.onError(RuntimeException("Server error"))
                        else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                    }
                    return
                }

                val body = response.body()
                if (body == null) {
                    callback.onError(RuntimeException("body is null"))
                } else {
                    callback.onSuccess(body)
                }
            }
            override fun onFailure(call: Call<List<Post>>, e: Throwable) {
                callback.onError(e)
            }
        })
    }

    override fun likeById(
        id: Long,
        likedByMe: Boolean,
        callback: PostRepository.PostCallback<Post>
    ) {
        val call = if (likedByMe) {
            PostApi.service.dislikeByID(id)
        } else {
            PostApi.service.likeByID(id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    when (response.code()) {
                        404 -> callback.onError(RuntimeException("Post not found"))
                        500 -> callback.onError(RuntimeException("Server error"))
                        else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                    }
                    return
                }

                val body = response.body()
                if (body == null) {
                    callback.onError(RuntimeException("body is null"))
                } else {
                    callback.onSuccess(body)
                }
            }

            override fun onFailure(call: Call<Post>, e: Throwable) {
                callback.onError(e)
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.PostCallback<Post>) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> callback.onError(RuntimeException("Post not found"))
                            500 -> callback.onError(RuntimeException("Server error"))
                            else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }

                    val body = response.body()
                    if (body == null) {
                        callback.onError(RuntimeException("body is null"))
                    } else {
                        callback.onSuccess(body)
                    }
                }

                override fun onFailure(call: Call<Post>, e: Throwable) {
                    callback.onError(e)
                }
            })
    }

    override fun removeById(
        id: Long,
        callback: PostRepository.PostCallback<Unit>
    ) {
        PostApi.service.removeByID(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        when (response.code()) {
                            404 -> callback.onError(RuntimeException("Post not found"))
                            500 -> callback.onError(RuntimeException("Server error"))
                            else -> callback.onError(RuntimeException("Error: ${response.code()}"))
                        }
                        return
                    }
                    callback.onSuccess(Unit)
                }

                override fun onFailure(call: Call<Unit>, e: Throwable) {
                    callback.onError(e)
                }
            })
    }
}
