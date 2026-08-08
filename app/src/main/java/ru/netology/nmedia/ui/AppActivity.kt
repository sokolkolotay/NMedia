package ru.netology.nmedia.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityAppBinding

class AppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppBinding
    private val viewModel: AlbumViewModel by viewModels()
    private val tracksAdapter = TracksAdapter { trackId ->
        viewModel.toggleTrack(trackId)
    }.apply {
        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tracks.apply {
            layoutManager = LinearLayoutManager(this@AppActivity)
            adapter = tracksAdapter
            setHasFixedSize(true)
        }

        binding.albumControl.setOnClickListener {
            viewModel.toggleAlbumPlayback()
        }
        binding.previousButton.setOnClickListener {
            viewModel.playPrevious()
        }
        binding.nextButton.setOnClickListener {
            viewModel.playNext()
        }
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }

        viewModel.state.observe(this, ::render)
    }

    private fun render(state: AlbumUiState) = with(binding) {
        loadingIndicator.isVisible = state.isAlbumLoading
        content.isVisible = state.album != null
        errorPanel.isVisible = !state.isAlbumLoading && state.album == null

        val album = state.album
        if (album == null) {
            if (!state.isAlbumLoading) {
                errorDetails.text = getString(
                    R.string.load_error_details,
                    state.errorMessage.orEmpty(),
                )
            }
            return@with
        }

        albumTitle.text = album.title
        albumArtist.text = album.artist
        val trackCount = resources.getQuantityString(
            R.plurals.track_count,
            album.tracks.size,
            album.tracks.size,
        )
        albumMeta.text = getString(
            R.string.album_meta,
            album.published,
            album.genre,
            trackCount,
        )

        val activePlayback = state.isPlaying || state.isBuffering
        albumControl.setText(if (activePlayback) R.string.pause else R.string.play)
        albumControl.setIconResource(
            if (activePlayback) R.drawable.ic_pause_24 else R.drawable.ic_play_24
        )
        headerProgress.isVisible = state.isBuffering

        val selectedTrack = album.tracks.find { it.id == state.selectedTrackId }
        playbackStatus.text = when {
            state.errorMessage != null -> getString(
                R.string.playback_error,
                state.errorMessage,
            )
            selectedTrack == null -> getString(R.string.ready_to_play)
            state.isBuffering -> getString(R.string.buffering_track, selectedTrack.title)
            state.isPlaying -> getString(R.string.now_playing_track, selectedTrack.title)
            else -> getString(R.string.paused_track, selectedTrack.title)
        }

        tracksAdapter.submitList(
            album.tracks.map { track ->
                TrackItem(
                    id = track.id,
                    title = track.title,
                    albumTitle = album.title,
                    isCurrent = track.id == state.selectedTrackId,
                    isPlaying = track.id == state.selectedTrackId && state.isPlaying,
                    isBuffering = track.id == state.selectedTrackId && state.isBuffering,
                )
            }
        )
    }
}
