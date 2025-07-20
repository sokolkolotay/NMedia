package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import ru.netology.nmedia.R
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.FragmentSinglePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class SinglePostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSinglePostBinding.inflate(inflater, container, false)

        val cardBinding = CardPostBinding.bind(binding.cardContainer.root)

        val interactionListener = object : OnInteractionListener {
            override fun onLikeById(post: Post) = viewModel.like(post.id)
            override fun onRemoveById(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_singlePostFragment_to_newPostFragment,
                    Bundle().apply {
                        textArgs = post.content
                    }
                )
            }
            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
                viewModel.repost(post.id)
            }
            override fun onPlayVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, post.video?.toUri())
                startActivity(intent)
            }
            override fun onOpenPost(post: Post) { /* Не нужен */ }
        }

        val holder = PostViewHolder(cardBinding, interactionListener)

        val postId = requireArguments().getLong(ARG_POST_ID)
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: return@observe
            holder.bind(post)
        }

        return binding.root
    }

    companion object {
        private const val ARG_POST_ID = "postId"
        fun createBundle(postId: Long) = Bundle().apply { putLong(ARG_POST_ID, postId) }
    }
}