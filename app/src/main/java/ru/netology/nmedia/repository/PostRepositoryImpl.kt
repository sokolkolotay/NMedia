package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val db: AppDb,
    private val api: PostApiService,
    private val client: OkHttpClient,
) : PostRepository {

    private val gson = Gson()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private const val PAGE_SIZE = 10
        private val jsonType = "application/json".toMediaType()
    }

    override val data = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        remoteMediator = PostRemoteMediator(api, db),
        pagingSourceFactory = dao::pagingSource,
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
    }

    override suspend fun save(post: Post) {
        val localId = dao.insert(
            PostEntity.fromDto(post).copy(isSynced = false)
        )

        try {
            val request = Request.Builder()
                .post(
                    gson.toJson(post.copy(id = 0))
                        .toRequestBody(jsonType)
                )
                .url("$BASE_URL/api/slow/posts")
                .build()

            val serverPost = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw RuntimeException("HTTP ${response.code}")
                    }
                    val body = response.body?.string()
                        ?: throw RuntimeException("body is null")
                    gson.fromJson(body, Post::class.java)
                }
            }

            dao.updateIdAndSynced(localId, serverPost.id)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean): Post {
        dao.likeById(id)

        val request = if (likedByMe) {
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
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw RuntimeException("HTTP ${response.code}")
                    }
                    val body = response.body?.string()
                        ?: throw RuntimeException("body is null")
                    gson.fromJson(body, Post::class.java)
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

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("HTTP ${response.code}")
                }
            }
        }
    }
}
