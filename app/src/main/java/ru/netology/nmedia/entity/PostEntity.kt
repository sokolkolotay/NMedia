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
        id, authorId, author, content, published, likedByMe, ownedByMe, likes, isSynced,
        attachment = if (attachmentUrl != null && attachmentType != null)
            Attachment(attachmentUrl, attachmentType)
        else null
    )

    companion object {
        fun fromDto(dto: Post, hidden: Boolean = false) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.ownedByMe,
                dto.likes,
                dto.isSynced,
                hidden,
                attachmentUrl = dto.attachment?.url,
                attachmentType = dto.attachment?.type,
            )
    }
}
