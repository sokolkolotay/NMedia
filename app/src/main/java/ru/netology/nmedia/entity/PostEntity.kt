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
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val isSynced: Boolean = true,
    val hidden: Boolean = false,
    val attachmentUrl: String? = null,
    val attachmentType: AttachmentType? = null,
) {
    fun toDto() = Post(
        id, author, content, published, likedByMe, likes, isSynced,
        attachment = if (attachmentUrl != null && attachmentType != null)
            Attachment(attachmentUrl, attachmentType)
        else null
    )

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
                attachmentUrl = dto.attachment?.url,
                attachmentType = dto.attachment?.type,
            )
    }
}