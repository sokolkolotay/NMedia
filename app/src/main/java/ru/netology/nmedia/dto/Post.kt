package ru.netology.nmedia.dto

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE,
}

data class Post(
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
    val attachment: Attachment? = null,
)
