package ru.netology.nmedia.dto

data class Post(
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int,
    val comments: Int = 150_435,
    val reposts: Int,
    val views: Int = 9_599_211,
    val likeByMe: Boolean = false
)
