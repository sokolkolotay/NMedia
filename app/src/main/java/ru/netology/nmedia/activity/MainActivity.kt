package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import kotlin.math.floor

fun formatCount(count: Int): String {
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

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(
            onLikeListener = { post -> viewModel.like(post.id) },
            onShareListener = { post -> viewModel.repost(post.id) }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this)
        { posts ->
            adapter.submitList(posts)
        }
    }
}
