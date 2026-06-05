package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    // Показываем только не скрытые посты
    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    fun updateContentById(id: Long, content: String)

    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("UPDATE PostEntity SET isSynced = :isSynced WHERE id = :id")
    fun updateSyncedById(id: Long, isSynced: Boolean)

    @Query("UPDATE PostEntity SET id = :newId, isSynced = 1 WHERE id = :localId")
    fun updateIdAndSynced(localId: Long, newId: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Long)

    // Вставить новые посты как скрытые (hidden = true)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertHidden(posts: List<PostEntity>)

    // Сколько скрытых постов ждут показа
    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    fun getNewerCount(): LiveData<Int>

    // Показать все скрытые посты
    @Query("UPDATE PostEntity SET hidden = 0 WHERE hidden = 1")
    fun showAll()

    // Максимальный id в БД — чтобы не запрашивать уже имеющиеся посты
    @Query("SELECT MAX(id) FROM PostEntity")
    fun getMaxId(): Long?
}