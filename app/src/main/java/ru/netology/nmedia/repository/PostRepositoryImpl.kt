package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.util.concurrent.TimeUnit

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    val posts: List<Post> = gson.fromJson(body, typeToken.type)
                    // Сохраняем в локальную БД
                    dao.insert(posts.map { PostEntity.fromDto(it) })
                    callback.onSuccess(posts)
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    override suspend fun save(post: Post) {
        // Шаг 1: сохраняем локально с isSynced = false — пост сразу виден в списке
        val localId = dao.insert(PostEntity.fromDto(post).copy(isSynced = false))

        // Шаг 2: отправляем на сервер
        try {
            val request = Request.Builder()
                .post(gson.toJson(post.copy(id = 0)).toRequestBody(jsonType))
                .url("$BASE_URL/api/slow/posts")
                .build()

            val serverPost = client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: throw RuntimeException("body is null")
                gson.fromJson(body, Post::class.java)
            }

            // Шаг 3: заменяем локальный id на серверный и помечаем как синхронизированный
            dao.updateIdAndSynced(localId, serverPost.id)
        } catch (e: IOException) {
            // Пост остаётся в БД с isSynced = false — пользователь видит иконку статуса
            // и может нажать Retry
            throw e
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean): Post {
        // Шаг 1: обновляем локально
        dao.likeById(id)

        // Шаг 2: отправляем в сеть
        val request = if (likedByMe) {
            Request.Builder().delete().url("$BASE_URL/api/posts/$id/likes").build()
        } else {
            Request.Builder().post("".toRequestBody()).url("$BASE_URL/api/posts/$id/likes").build()
        }

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: throw RuntimeException("body is null")
                gson.fromJson(body, Post::class.java)
            }
        } catch (e: IOException) {
            // Откатываем локальное изменение
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun removeById(id: Long) {
        // Шаг 1: удаляем локально
        dao.removeById(id)

        // Шаг 2: удаляем на сервере
        val request = Request.Builder()
            .delete()
            .url("$BASE_URL/api/slow/posts/$id")
            .build()

        client.newCall(request).execute().close()
    }
}