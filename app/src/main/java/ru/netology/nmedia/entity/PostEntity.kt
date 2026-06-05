package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val isSynced: Boolean = true,   // false = ещё не сохранён на сервере
    val hidden: Boolean = false,    // true = новый пост, ещё не показан пользователю
) {
    fun toDto() = Post(id, author, content, published, likedByMe, likes, isSynced)

    companion object {
        fun fromDto(dto: Post, hidden: Boolean = false) =
            PostEntity(
                dto.id,
                dto.author,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.isSynced,
                hidden,
            )
    }
}