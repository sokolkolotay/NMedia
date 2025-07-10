package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String? = null,
    val likes: Int,
    val comments: Int = 150_435,
    val reposts: Int,
    val views: Int = 9_599_211,
    val likeByMe: Boolean = false,
    val video: String? = null
)
