package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long)
}
