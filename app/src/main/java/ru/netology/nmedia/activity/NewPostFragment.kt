package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.activity.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class NewPostFragment : Fragment() {

    private var bindingMain: FragmentNewPostBinding? = null
    private val binding
        get() = bindingMain!!

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingMain = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        val initialContent = arguments?.textArgs ?: viewModel.draft ?: ""
        viewModel.startEditing(initialContent)

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (binding.edit.text.toString() != post.content) {
                binding.edit.setText(post.content)
            }
        }

        binding.save.setOnClickListener {
            if (binding.edit.text.isNotBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
                viewModel.clearDraft()//очищаем черновик
            }
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        val content = binding.edit.text.toString()
        viewModel.saveDraft(content)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingMain = null
    }
}