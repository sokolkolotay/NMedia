package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("UPDATE PostEntity SET isSynced = :isSynced WHERE id = :id")
    suspend fun updateSyncedById(id: Long, isSynced: Boolean)

    @Query("UPDATE PostEntity SET id = :newId, isSynced = 1 WHERE id = :localId")
    suspend fun updateIdAndSynced(localId: Long, newId: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHidden(posts: List<PostEntity>)

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    fun getNewerCount(): Flow<Int>

    @Query("UPDATE PostEntity SET hidden = 0 WHERE hidden = 1")
    suspend fun showAll()

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getMaxId(): Long?
}