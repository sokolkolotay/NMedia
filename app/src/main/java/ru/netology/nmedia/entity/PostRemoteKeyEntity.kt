package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: String,
    val postId: Long,
) {
    companion object {
        const val AFTER = "AFTER"
        const val BEFORE = "BEFORE"
    }
}
