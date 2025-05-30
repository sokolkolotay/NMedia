package ru.netology.nmedia.dto

data class Post(
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    var likes: Int = 999,
    var comments: Int = 150_435,
    var reposts: Int = 6_399,
    var views: Int = 9_599_211,
    var likeByMe: Boolean = false
)
