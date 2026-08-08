package ru.netology.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import ru.netology.nmedia.model.Place

class SharedPreferencesPlaceRepository(context: Context) : PlaceRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override fun getAll(): List<Place> = runCatching {
        val json = preferences.getString(PLACES_KEY, null) ?: return emptyList()
        val array = JSONArray(json)
        buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    Place(
                        id = item.getLong(ID_KEY),
                        title = item.getString(TITLE_KEY),
                        description = item.optString(DESCRIPTION_KEY),
                        latitude = item.getDouble(LATITUDE_KEY),
                        longitude = item.getDouble(LONGITUDE_KEY),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    override fun getById(id: Long): Place? = getAll().find { it.id == id }

    override fun save(place: Place): Place {
        val places = getAll().toMutableList()
        val saved = if (place.id == 0L) {
            place.copy(id = (places.maxOfOrNull(Place::id) ?: 0L) + 1L)
        } else {
            place
        }

        val index = places.indexOfFirst { it.id == saved.id }
        if (index == -1) {
            places.add(saved)
        } else {
            places[index] = saved
        }
        write(places)
        return saved
    }

    override fun deleteById(id: Long) {
        write(getAll().filterNot { it.id == id })
    }

    private fun write(places: List<Place>) {
        val array = JSONArray()
        places.forEach { place ->
            array.put(
                JSONObject()
                    .put(ID_KEY, place.id)
                    .put(TITLE_KEY, place.title)
                    .put(DESCRIPTION_KEY, place.description)
                    .put(LATITUDE_KEY, place.latitude)
                    .put(LONGITUDE_KEY, place.longitude),
            )
        }
        preferences.edit {
            putString(PLACES_KEY, array.toString())
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "saved_places"
        const val PLACES_KEY = "places"
        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"
        const val LATITUDE_KEY = "latitude"
        const val LONGITUDE_KEY = "longitude"
    }
}
