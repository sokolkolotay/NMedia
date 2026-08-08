package ru.netology.nmedia.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ItemTrackBinding

data class TrackItem(
    val id: Long,
    val title: String,
    val albumTitle: String,
    val isCurrent: Boolean,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
)

class TracksAdapter(
    private val onTrackClick: (Long) -> Unit,
) : ListAdapter<TrackItem, TracksAdapter.TrackViewHolder>(TrackDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TrackViewHolder(binding, onTrackClick)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TrackViewHolder(
        private val binding: ItemTrackBinding,
        private val onTrackClick: (Long) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrackItem) = with(binding) {
            root.isActivated = item.isCurrent
            trackNumber.text = String.format(Locale.getDefault(), "%02d", item.id)
            trackTitle.text = item.title
            trackAlbum.text = item.albumTitle

            trackProgress.isVisible = item.isBuffering
            trackButton.isVisible = !item.isBuffering
            trackButton.setImageResource(
                if (item.isCurrent && item.isPlaying) {
                    R.drawable.ic_pause_24
                } else {
                    R.drawable.ic_play_24
                }
            )
            trackButton.contentDescription = root.context.getString(
                if (item.isCurrent && item.isPlaying) {
                    R.string.pause_track
                } else {
                    R.string.play_track
                },
                item.title,
            )

            nowPlaying.isVisible = item.isCurrent
            nowPlaying.setText(
                when {
                    item.isBuffering -> R.string.buffering
                    item.isPlaying -> R.string.now_playing
                    else -> R.string.paused
                }
            )

            root.setOnClickListener { onTrackClick(item.id) }
            trackButton.setOnClickListener { onTrackClick(item.id) }
        }
    }

    private object TrackDiffCallback : DiffUtil.ItemCallback<TrackItem>() {
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean =
            oldItem == newItem
    }
}
