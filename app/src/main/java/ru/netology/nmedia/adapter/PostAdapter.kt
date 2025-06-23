package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.formatCount


interface OnInteractionListener {
    fun onLikeById(post: Post)
    fun onRemoveById(post: Post)
    fun onEdit(post: Post)
    fun onRepostById(post: Post)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) = with(binding) {
        author.text = post.author
        content.text = post.content
        published.text = post.published
        favorite.apply {
            isChecked = post.likeByMe
            text = formatCount(post.likes)
        }
        repost.apply {
            text = formatCount(post.reposts)
        }
        views.apply {
            text = formatCount(post.views)
        }

        favorite.setOnClickListener {
            onInteractionListener.onLikeById(post)
        }

        repost.setOnClickListener {
            onInteractionListener.onRepostById(post)
        }

        menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_options)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractionListener.onRemoveById(post)
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
    }
}

object PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}