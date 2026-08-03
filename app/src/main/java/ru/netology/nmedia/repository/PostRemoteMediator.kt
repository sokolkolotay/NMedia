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
    ): MediatorResult = try {
        val keyDao = db.postRemoteKeyDao()

        val response = when (loadType) {
            LoadType.REFRESH -> api.getLatest(state.config.initialLoadSize)
            LoadType.PREPEND -> {
                val id = keyDao.get(PostRemoteKeyEntity.AFTER)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                api.getAfter(id, state.config.pageSize)
            }
            LoadType.APPEND -> {
                val id = keyDao.get(PostRemoteKeyEntity.BEFORE)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                api.getBefore(id, state.config.pageSize)
            }
        }

        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code()}: ${response.message()}")
        }

        val posts = response.body()
            ?: throw RuntimeException("Response body is null")

        db.withTransaction {
            if (loadType == LoadType.REFRESH) {
                keyDao.removeAll()
                db.postDao().removeAll()
            }

            if (posts.isNotEmpty()) {
                when (loadType) {
                    LoadType.REFRESH -> {
                        keyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.AFTER,
                                posts.first().id,
                            )
                        )
                        keyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.BEFORE,
                                posts.last().id,
                            )
                        )
                    }
                    LoadType.PREPEND -> keyDao.insert(
                        PostRemoteKeyEntity(
                            PostRemoteKeyEntity.AFTER,
                            posts.first().id,
                        )
                    )
                    LoadType.APPEND -> keyDao.insert(
                        PostRemoteKeyEntity(
                            PostRemoteKeyEntity.BEFORE,
                            posts.last().id,
                        )
                    )
                }

                db.postDao().insert(
                    posts.map { PostEntity.fromDto(it.copy(isSynced = true)) }
                )
            }
        }

        MediatorResult.Success(endOfPaginationReached = posts.isEmpty())
    } catch (e: Exception) {
        MediatorResult.Error(e)
    }
}
