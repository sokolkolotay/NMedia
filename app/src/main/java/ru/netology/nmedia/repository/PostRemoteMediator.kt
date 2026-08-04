package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val api: PostApiService,
    private val db: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        // Новые посты загружаются только явным REFRESH, а не скроллом вверх.
        if (loadType == LoadType.PREPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val postDao = db.postDao()
            val keyDao = db.postRemoteKeyDao()
            val topId = if (loadType == LoadType.REFRESH) postDao.getMaxId() else null

            val response = when (loadType) {
                LoadType.REFRESH -> if (topId == null) {
                    api.getLatest(state.config.initialLoadSize)
                } else {
                    api.getAfter(topId, state.config.initialLoadSize)
                }
                LoadType.APPEND -> {
                    val bottomId = keyDao.get(PostRemoteKeyEntity.BEFORE)
                        ?: postDao.getMinId()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    api.getBefore(bottomId, state.config.pageSize)
                }
                LoadType.PREPEND -> error("PREPEND is disabled")
            }

            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code()}: ${response.message()}")
            }

            val posts = response.body()
                ?: throw RuntimeException("Response body is null")

            db.withTransaction {
                if (posts.isNotEmpty()) {
                    postDao.insert(
                        posts.map { PostEntity.fromDto(it.copy(isSynced = true)) }
                    )

                    when (loadType) {
                        LoadType.REFRESH -> {
                            // При первом запуске запоминаем нижнюю границу кеша.
                            // При следующих REFRESH она не меняется: новые посты
                            // лишь добавляются над уже сохранёнными.
                            if (keyDao.get(PostRemoteKeyEntity.BEFORE) == null) {
                                postDao.getMinId()?.let { bottomId ->
                                    keyDao.insert(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.BEFORE,
                                            bottomId,
                                        )
                                    )
                                }
                            }
                        }
                        LoadType.APPEND -> keyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.BEFORE,
                                posts.last().id,
                            )
                        )
                        LoadType.PREPEND -> Unit
                    }
                }
            }

            MediatorResult.Success(
                endOfPaginationReached =
                    loadType == LoadType.APPEND && posts.isEmpty()
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
