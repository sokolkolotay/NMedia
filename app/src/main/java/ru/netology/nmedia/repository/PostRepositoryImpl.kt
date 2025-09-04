package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    //cтарая реализация через thread с использованием .execute()
//    override fun getAll(): List<Post> {
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, typeToken.type)
//            }
//    }

    //новая реализация через OkHttp с использованием .enqueue
    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val posts =
                            response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(posts, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

//    override fun likeById(id: Long, likedByMe: Boolean): Post {
//
//        val request = if (likedByMe) {
//            Request.Builder()
//                .delete()
//                .url("${BASE_URL}/api/posts/$id/likes")
//                .build()
//        } else {
//            Request.Builder()
//                .post("".toRequestBody())
//                .url("${BASE_URL}/api/posts/$id/likes")
//                .build()
//        }
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let { gson.fromJson(it, Post::class.java) }
//    }

    override fun likeByIdAsync(
        id: Long,
        likedByMe: Boolean,
        callback: PostRepository.LikeByIdAsync
    ) {
        val request = if (likedByMe) {
            Request.Builder()
                .delete()
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
        } else {
            Request.Builder()
                .post("".toRequestBody())
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
        }

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val posts =
                            response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(posts, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}
