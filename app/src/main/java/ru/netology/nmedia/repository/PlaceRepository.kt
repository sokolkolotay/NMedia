package ru.netology.nmedia.repository

import ru.netology.nmedia.model.Place

interface PlaceRepository {
    fun getAll(): List<Place>
    fun getById(id: Long): Place?
    fun save(place: Place): Place
    fun deleteById(id: Long)
}

