package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.signIn.isEnabled = !it
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.errorText.isVisible = it != null
            binding.errorText.text = it
        }

        viewModel.success.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.signIn.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            if (login.isBlank() || pass.isBlank()) {
                binding.errorText.isVisible = true
                binding.errorText.text = "Заполните все поля"
                return@setOnClickListener
            }
            viewModel.signIn(login, pass)
        }

        return binding.root
    }
}
