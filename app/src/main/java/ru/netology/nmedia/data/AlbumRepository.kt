package ru.netology.nmedia.data

import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class AlbumRepository(
    private val client: OkHttpClient = OkHttpClient(),
) {
    private var activeCall: Call? = null

    fun loadAlbum(callback: (Result<Album>) -> Unit) {
        activeCall?.cancel()

        val request = Request.Builder()
            .url(ALBUM_URL)
            .build()

        activeCall = client.newCall(request).also { call ->
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!call.isCanceled()) {
                        callback(Result.failure(e))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = runCatching {
                        response.use {
                            if (!it.isSuccessful) {
                                throw IOException("HTTP ${it.code}")
                            }
                            parseAlbum(it.body.string())
                        }
                    }
                    callback(result)
                }
            })
        }
    }

    fun cancel() {
        activeCall?.cancel()
        activeCall = null
    }

    private fun parseAlbum(source: String): Album {
        val json = JSONObject(source)
        val jsonTracks = json.getJSONArray("tracks")
        val tracks = buildList(jsonTracks.length()) {
            repeat(jsonTracks.length()) { index ->
                val track = jsonTracks.getJSONObject(index)
                add(
                    Track(
                        id = track.getLong("id"),
                        file = track.getString("file"),
                    )
                )
            }
        }

        require(tracks.isNotEmpty()) { "Album has no tracks" }

        return Album(
            id = json.getLong("id"),
            title = json.getString("title"),
            subtitle = json.getString("subtitle"),
            artist = json.getString("artist"),
            published = json.getString("published"),
            genre = json.getString("genre"),
            tracks = tracks,
        )
    }

    companion object {
        const val ALBUM_URL =
            "https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/album.json"
        const val TRACKS_BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"

        fun trackUrl(file: String): String = TRACKS_BASE_URL + file
    }
}
