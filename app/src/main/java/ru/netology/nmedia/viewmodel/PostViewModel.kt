package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = 0,
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    val data = repository.data.cachedIn(viewModelScope)
    val authState = appAuth.state.asLiveData()

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _dataState = MutableLiveData<FeedModel>()
    val dataState: LiveData<FeedModel>
        get() = _dataState

    private val _authError = SingleLiveEvent<Unit>()
    val authError: LiveData<Unit>
        get() = _authError

    fun like(post: Post) {
        if (!appAuth.state.value.isAuthorized) {
            _authError.value = Unit
            return
        }

        viewModelScope.launch {
            try {
                repository.likeById(post.id, post.likedByMe)
            } catch (e: Exception) {
                _dataState.value = FeedModel(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModel(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    repository.save(post)
                    _postCreated.postValue(Unit)
                } catch (e: Exception) {
                    _dataState.value = FeedModel(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun retrySave(post: Post) {
        viewModelScope.launch {
            try {
                repository.save(post)
            } catch (e: Exception) {
                _dataState.value = FeedModel(error = true)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) return
        edited.value = edited.value?.copy(content = text)
    }
}
