package ru.netology.nmedia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import ru.netology.nmedia.data.Album
import ru.netology.nmedia.data.AlbumRepository

data class AlbumUiState(
    val isAlbumLoading: Boolean = true,
    val album: Album? = null,
    val selectedTrackId: Long? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
)

class AlbumViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlbumRepository()
    private val player = ExoPlayer.Builder(application.applicationContext).build().apply {
        repeatMode = Player.REPEAT_MODE_ALL
    }

    private val _state = MutableLiveData(AlbumUiState())
    val state: LiveData<AlbumUiState> = _state

    private var album: Album? = null
    private var selectedTrackId: Long? = null
    private var isAlbumLoading = true
    private var errorMessage: String? = null

    init {
        player.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (selectedTrackId != null) {
                        selectedTrackId = mediaItem?.mediaId?.toLongOrNull()
                    }
                    publishState()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    publishState()
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    publishState()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    publishState()
                }

                override fun onPlayerError(error: PlaybackException) {
                    errorMessage = error.errorCodeName
                    publishState()
                }
            }
        )
        loadAlbum()
    }

    fun retry() {
        if (!isAlbumLoading) {
            loadAlbum()
        }
    }

    fun toggleAlbumPlayback() {
        val tracks = album?.tracks.orEmpty()
        if (tracks.isEmpty()) return

        val id = selectedTrackId ?: tracks.first().id
        toggleTrack(id)
    }

    fun toggleTrack(trackId: Long) {
        val loadedAlbum = album ?: return
        val index = loadedAlbum.tracks.indexOfFirst { it.id == trackId }
        if (index == -1) return

        if (selectedTrackId == trackId && player.playWhenReady) {
            player.pause()
            return
        }

        errorMessage = null
        selectedTrackId = trackId

        if (player.currentMediaItemIndex != index) {
            player.seekToDefaultPosition(index)
        }
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        player.play()
        publishState()
    }

    fun playPrevious() = playRelative(-1)

    fun playNext() = playRelative(1)

    private fun playRelative(delta: Int) {
        val tracks = album?.tracks.orEmpty()
        if (tracks.isEmpty()) return

        val currentIndex = tracks.indexOfFirst { it.id == selectedTrackId }
            .takeIf { it >= 0 }
            ?: 0
        val targetIndex = (currentIndex + delta).mod(tracks.size)
        val target = tracks[targetIndex]

        errorMessage = null
        selectedTrackId = target.id
        player.seekToDefaultPosition(targetIndex)
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        player.play()
        publishState()
    }

    private fun loadAlbum() {
        isAlbumLoading = true
        errorMessage = null
        publishState()

        repository.loadAlbum { result ->
            viewModelScope.launch {
                result.fold(
                    onSuccess = { loadedAlbum ->
                        album = loadedAlbum
                        selectedTrackId = null
                        isAlbumLoading = false
                        errorMessage = null
                        player.setMediaItems(
                            loadedAlbum.tracks.map { track ->
                                MediaItem.Builder()
                                    .setMediaId(track.id.toString())
                                    .setUri(AlbumRepository.trackUrl(track.file))
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(track.title)
                                            .setArtist(loadedAlbum.artist)
                                            .setAlbumTitle(loadedAlbum.title)
                                            .build()
                                    )
                                    .build()
                            }
                        )
                        publishState()
                    },
                    onFailure = { error ->
                        isAlbumLoading = false
                        errorMessage = error.message ?: error.javaClass.simpleName
                        publishState()
                    },
                )
            }
        }
    }

    private fun publishState() {
        _state.value = AlbumUiState(
            isAlbumLoading = isAlbumLoading,
            album = album,
            selectedTrackId = selectedTrackId,
            isPlaying = player.isPlaying,
            isBuffering = selectedTrackId != null &&
                player.playWhenReady &&
                player.playbackState == Player.STATE_BUFFERING,
            errorMessage = errorMessage,
        )
    }

    override fun onCleared() {
        repository.cancel()
        player.release()
    }
}
