package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    fun save(post: PostEntity) {
        if (post.id == 0L) {
            insert(post)
        } else {
            post.content?.let { updateById(post.id, it) }
        }
    }

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content=:content WHERE id=:id")
    fun updateById(id: Long, content: String)

    @Query("""
        UPDATE PostEntity SET 
        likes = likes + CASE WHEN likeByMe THEN -1 ELSE 1 END,
        likeByMe = CASE WHEN likeByMe THEN 0 ELSE 1 END
        WHERE id =:id;
    """)
    fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id =:id")
    fun removeById(id: Long)

    @Query("UPDATE PostEntity SET reposts = reposts + 1 WHERE id =:id")
    fun repost(id: Long)
}