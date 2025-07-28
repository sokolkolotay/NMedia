package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity(tableName = "PostEntity")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val published: String,
    val content: String? = null,
    val likes: Int = 0,
    val comments: Int = 150_435,
    val reposts: Int,
    val views: Int = 9_599_211,
    val likeByMe: Boolean = false,
    val video: String? = null
) {
    fun toDto() = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likes = likes,
        comments = comments,
        reposts = reposts,
        views = views,
        likeByMe = likeByMe,
        video = video
    )

    companion object {
        fun fromDto(post: Post) = post.run {
            PostEntity(
                id = id,
                author = author,
                published = published,
                content = content,
                likes = likes,
                comments = comments,
                reposts = reposts,
                views = views,
                likeByMe = likeByMe,
                video = video
            )
        }
    }


}
