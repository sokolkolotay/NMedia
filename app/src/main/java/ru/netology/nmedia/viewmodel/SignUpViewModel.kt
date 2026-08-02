package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val api: PostApiService,
    private val appAuth: AppAuth
) : ViewModel() {


    private val _loading =
        MutableLiveData(false)

    val loading: LiveData<Boolean>
        get() = _loading



    private val _error =
        MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error



    val success =
        SingleLiveEvent<Unit>()



    fun signUp(
        login: String,
        pass: String,
        name: String
    ) {

        viewModelScope.launch {

            _loading.value = true


            try {

                val response =
                    api.signUp(
                        login,
                        pass,
                        name
                    )


                if (!response.isSuccessful) {

                    _error.value =
                        "Ошибка регистрации"

                    return@launch
                }


                val token =
                    response.body()
                        ?: throw Exception("Empty body")


                appAuth.setAuth(
                    token.id,
                    token.token
                )


                success.value = Unit


            } catch (e: Exception) {

                _error.value =
                    "Ошибка сети"


            } finally {

                _loading.value = false
            }
        }
    }
}