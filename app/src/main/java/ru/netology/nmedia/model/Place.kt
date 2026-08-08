package ru.netology.nmedia.model

data class Place(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)

