package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) = viewModel.edit(post)

            override fun onLike(post: Post) = viewModel.like(post)

            override fun onRemove(post: Post) = viewModel.removeById(post.id)

            override fun onShare(post: Post) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                )
            }

            override fun onRetrySave(post: Post) = viewModel.retrySave(post)

            override fun onImage(url: String) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply { putString("url", url) },
                )
            }
        })

        binding.list.adapter = adapter
        binding.newPostsBanner.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.data.collectLatest(adapter::submitData)
                }
                launch {
                    adapter.loadStateFlow.collectLatest { state ->
                        val refresh = state.refresh
                        binding.progress.isVisible = refresh is LoadState.Loading
                        binding.swipeRefresh.isRefreshing = refresh is LoadState.Loading
                        binding.errorGroup.isVisible = refresh is LoadState.Error
                        binding.emptyText.isVisible =
                            refresh is LoadState.NotLoading && adapter.itemCount == 0
                    }
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)
        binding.retryButton.setOnClickListener { adapter.retry() }

        // Повторный запрос первой страницы после login/logout.
        viewModel.authState.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        viewModel.authError.observe(viewLifecycleOwner) {
            AlertDialog.Builder(requireContext())
                .setTitle("Требуется авторизация")
                .setMessage("Войдите, чтобы взаимодействовать с постами")
                .setPositiveButton("Войти") { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        return binding.root
    }
}
