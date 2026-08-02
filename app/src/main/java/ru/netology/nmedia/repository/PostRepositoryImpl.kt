package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val client: OkHttpClient
) : PostRepository {

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override val data: Flow<List<Post>> =
        dao.getAll().map { entities ->
            entities.map { it.toDto() }
        }


    override suspend fun getAll() {

        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()

        val posts = withContext(Dispatchers.IO) {

            client.newCall(request)
                .execute()
                .use { response ->

                    val body = response.body?.string()
                        ?: throw RuntimeException("body is null")

                    gson.fromJson<List<Post>>(
                        body,
                        typeToken.type
                    )
                }
        }

        dao.insert(
            posts.map {
                PostEntity.fromDto(it)
            }
        )
    }


    override suspend fun getNewer(id: Long): Int {

        val request = Request.Builder()
            .url("$BASE_URL/api/posts/$id/newer")
            .build()

        return try {

            val posts = withContext(Dispatchers.IO) {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        if (!response.isSuccessful)
                            return@withContext emptyList()

                        val body = response.body?.string()
                            ?: return@withContext emptyList()

                        gson.fromJson<List<Post>>(
                            body,
                            typeToken.type
                        )
                    }
            }

            dao.insertHidden(
                posts.map {
                    PostEntity.fromDto(
                        it,
                        hidden = true
                    )
                }
            )

            posts.size

        } catch (e: Exception) {
            0
        }
    }


    override suspend fun showNewPosts() {
        dao.showAll()
    }


    override suspend fun save(post: Post) {

        val localId =
            dao.insert(
                PostEntity.fromDto(post)
                    .copy(isSynced = false)
            )

        try {

            val request = Request.Builder()
                .post(
                    gson.toJson(
                        post.copy(id = 0)
                    ).toRequestBody(jsonType)
                )
                .url("$BASE_URL/api/slow/posts")
                .build()


            val serverPost = withContext(Dispatchers.IO) {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val body =
                            response.body?.string()
                                ?: throw RuntimeException("body is null")

                        gson.fromJson(
                            body,
                            Post::class.java
                        )
                    }
            }

            dao.updateIdAndSynced(
                localId,
                serverPost.id
            )

        } catch (e: Exception) {
            throw e
        }
    }


    override suspend fun likeById(
        id: Long,
        likedByMe: Boolean
    ): Post {

        dao.likeById(id)

        val request =
            if (likedByMe) {

                Request.Builder()
                    .delete()
                    .url("$BASE_URL/api/posts/$id/likes")
                    .build()

            } else {

                Request.Builder()
                    .post("".toRequestBody())
                    .url("$BASE_URL/api/posts/$id/likes")
                    .build()
            }


        return try {

            withContext(Dispatchers.IO) {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val body =
                            response.body?.string()
                                ?: throw RuntimeException("body is null")

                        gson.fromJson(
                            body,
                            Post::class.java
                        )
                    }
            }

        } catch (e: Exception) {

            dao.likeById(id)
            throw e
        }
    }


    override suspend fun removeById(id: Long) {

        dao.removeById(id)

        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .delete()
                .url("$BASE_URL/api/slow/posts/$id")
                .build()

            client.newCall(request)
                .execute()
                .close()
        }
    }
}