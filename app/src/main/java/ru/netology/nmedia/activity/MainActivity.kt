package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post


class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    val postContentLauncher = registerForActivityResult(NewPostResultContract()) { content ->
        content ?: return@registerForActivityResult
        viewModel.changeContent(content)
        viewModel.save()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(
            object : OnInteractionListener {
                override fun onLikeById(post: Post) {
                    viewModel.like(post.id)
                }

                override fun onRemoveById(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    postContentLauncher.launch(post.content)
                }

                override fun onShare(post: Post) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }

                override fun onPlayVideo(post: Post) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                    val chooser = Intent.createChooser(
                        intent,
                        binding.root.context.getString(R.string.chooser_video_playback_application)
                    )
                    binding.root.context.startActivity(chooser)
                }
            }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val isNew = posts.size != adapter.itemCount
            adapter.submitList(posts) {
                if (isNew) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.fab.setOnClickListener {
            viewModel.clearEdited()
            postContentLauncher.launch(null)
        }

//
//        viewModel.edited.observe(this) { post ->
//            if (post.id != 0L) {
//                with(binding.content) {
//                    binding.oldContent.setText(post.content)
//                    setText(post.content)
//                    binding.editGroup.visibility = View.VISIBLE
//                    AndroidUtils.showKeyboard(this)
//                }
//            }
//        }
//
//        binding.cancelEdited.setOnClickListener {
//            with(binding.content) {
//                viewModel.clearEdited()
//                binding.editGroup.visibility = View.GONE
//                setText("")
//                clearFocus()
//                AndroidUtils.hideKeyboard(this)
//            }
//        }
//
//        binding.save.setOnClickListener {
//            with(binding.content) {
//                if (text.isNullOrBlank()) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        context.getString(R.string.error_empty_content),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
//
//                viewModel.changeContent(text.toString())
//                viewModel.save()
//
//                binding.editGroup.visibility = View.GONE
//                setText("")
//                clearFocus()
//                AndroidUtils.hideKeyboard(this)
//            }
//        }
    }
}
