package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import kotlin.math.floor

private fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> {
            val rounding = floor(count / 100.0) / 10
            "${rounding}K".replace(".0", "")
        }

        count < 1_000_000 -> "${count / 1_000}K"
        else -> {
            val rounding = floor(count / 100_000.0) / 10
            "${rounding}M".replace(".0", "")
        }
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                content.text = post.content
                published.text = post.published
                countFavorite.text = formatCount(post.likes)
                countComment.text = formatCount(post.comments)
                countRepost.text = formatCount(post.reposts)
                countView.text = formatCount(post.views)

                favorite.setImageResource(
                    if (post.likeByMe) {
                        R.drawable.ic_fill_favorite_24dp
                    } else {
                        R.drawable.ic_favorite_24dp
                    }
                )

                binding.favorite.setOnClickListener {
                    viewModel.like()
                }

                binding.repost.setOnClickListener {
                    viewModel.reposts()
                }
            }
        }
    }
}