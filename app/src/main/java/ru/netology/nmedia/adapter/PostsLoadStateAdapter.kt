package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemLoadStateBinding

class PostsLoadStateAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<PostsLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): PostsLoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PostsLoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(
        holder: PostsLoadStateViewHolder,
        loadState: LoadState,
    ) = holder.bind(loadState)
}

class PostsLoadStateViewHolder(
    private val binding: ItemLoadStateBinding,
    retry: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retry.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        binding.progress.isVisible = loadState is LoadState.Loading
        binding.errorText.isVisible = loadState is LoadState.Error
        binding.retry.isVisible = loadState is LoadState.Error
        binding.errorText.text = (loadState as? LoadState.Error)
            ?.error
            ?.localizedMessage
    }
}
