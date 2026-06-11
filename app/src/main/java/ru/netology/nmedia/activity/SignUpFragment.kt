package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel

class SignUpFragment : Fragment() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it
            binding.signUp.isEnabled = !it
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.errorText.isVisible = it != null
            binding.errorText.text = it
        }

        viewModel.success.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()

            if (name.isBlank() || login.isBlank() || pass.isBlank()) {
                binding.errorText.isVisible = true
                binding.errorText.text = "Заполните все поля"
                return@setOnClickListener
            }
            if (pass != confirmPass) {
                binding.errorText.isVisible = true
                binding.errorText.text = "Пароли не совпадают"
                return@setOnClickListener
            }
            viewModel.signUp(login, pass, name)
        }

        return binding.root
    }
}