package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.ItemTimeSeparatorBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedItem

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onRetrySave(post: Post) {}
    fun onImage(url: String) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {
    private var currentUserId: Long = 0

    fun setCurrentUserId(userId: Long) {
        if (currentUserId == userId) return
        currentUserId = userId
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is FeedItem.PostItem -> R.layout.card_post
        is FeedItem.SeparatorItem -> R.layout.item_time_separator
        null -> R.layout.card_post
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder = when (viewType) {
        R.layout.card_post -> {
            val binding = CardPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
            PostViewHolder(binding, onInteractionListener)
        }
        R.layout.item_time_separator -> {
            val binding = ItemTimeSeparatorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
            TimeSeparatorViewHolder(binding)
        }
        else -> error("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FeedItem.PostItem -> (holder as PostViewHolder)
                .bind(item.post, currentUserId)
            is FeedItem.SeparatorItem -> (holder as TimeSeparatorViewHolder)
                .bind(item.period)
            null -> Unit
        }
    }
}

class TimeSeparatorViewHolder(
    private val binding: ItemTimeSeparatorBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(period: FeedItem.Period) {
        binding.title.setText(
            when (period) {
                FeedItem.Period.TODAY -> R.string.period_today
                FeedItem.Period.YESTERDAY -> R.string.period_yesterday
                FeedItem.Period.LAST_WEEK -> R.string.period_last_week
            }
        )
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post, currentUserId: Long) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            Glide.with(avatar)
                .load(post.authorAvatar?.let { "http://10.0.2.2:9999/avatars/$it" })
                .placeholder(R.drawable.ic_netology_original_48dp)
                .error(R.drawable.ic_netology_original_48dp)
                .circleCrop()
                .into(avatar)
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"
            menu.isVisible = post.ownedByMe ||
                (currentUserId != 0L && post.authorId == currentUserId)

            syncStatus.isVisible = !post.isSynced
            retrySave.isVisible = !post.isSynced
            like.isEnabled = post.isSynced

            // Вложение
            if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
                attachment.isVisible = true
                Glide.with(attachment)
                    .load("http://10.0.2.2:9999/media/${post.attachment.url}")
                    .override(600, 300)
                    .centerCrop()
                    .into(attachment)
                attachment.setOnClickListener {
                    onInteractionListener.onImage("http://10.0.2.2:9999/media/${post.attachment.url}")
                }
            } else {
                attachment.isVisible = false
                attachment.setOnClickListener(null)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            retrySave.setOnClickListener {
                onInteractionListener.onRetrySave(post)
            }
        }
    }
}

class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        when {
            oldItem is FeedItem.PostItem && newItem is FeedItem.PostItem ->
                oldItem.post.id == newItem.post.id
            oldItem is FeedItem.SeparatorItem && newItem is FeedItem.SeparatorItem ->
                oldItem.period == newItem.period
            else -> false
        }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem
}
