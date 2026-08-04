package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long = 0,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val ownedByMe: Boolean = false,
    val likes: Int = 0,
    val isSynced: Boolean = true,
    val hidden: Boolean = false,
    val attachmentUrl: String? = null,
    val attachmentType: AttachmentType? = null,
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likedByMe = likedByMe,
        ownedByMe = ownedByMe,
        likes = likes,
        isSynced = isSynced,
        attachment = if (attachmentUrl != null && attachmentType != null)
            Attachment(attachmentUrl, attachmentType)
        else null
    )

    companion object {
        fun fromDto(dto: Post, hidden: Boolean = false) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                ownedByMe = dto.ownedByMe,
                likes = dto.likes,
                isSynced = dto.isSynced,
                hidden = hidden,
                attachmentUrl = dto.attachment?.url,
                attachmentType = dto.attachment?.type,
            )
    }
}
