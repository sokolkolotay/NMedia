package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    // Flow из репозитория преобразуем в LiveData через asLiveData()
    // UI подписывается на data и получает обновления автоматически при любом изменении БД
    val data: LiveData<FeedModel> = repository.data.map { posts ->
        FeedModel(posts = posts, empty = posts.isEmpty())
    }.asLiveData()

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> get() = _postCreated

    private val _dataState = MutableLiveData<FeedModel>()
    val dataState: LiveData<FeedModel> get() = _dataState

    val newerCount: LiveData<Int> = AppDb.getInstance(application)
        .postDao()
        .getNewerCount()
        .asLiveData()

    init {
        loadPosts()
        startPollingNewer()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _dataState.value = FeedModel(loading = true)
            try {
                repository.getAll()
                _dataState.value = FeedModel()
            } catch (e: Exception) {
                _dataState.value = FeedModel(error = true)
            }
        }
    }

    private fun startPollingNewer() {
        viewModelScope.launch {
            delay(10_000L)
            while (true) {
                val maxId = AppDb.getInstance(getApplication()).postDao().getMaxId() ?: 0L
                if (maxId > 0L) {
                    repository.getNewer(maxId)
                }
                delay(10_000L)
            }
        }
    }

    fun showNewPosts() {
        viewModelScope.launch {
            repository.showNewPosts()
        }
    }

    // Добавить в класс:
    private val _authError = SingleLiveEvent<Unit>()
    val authError: LiveData<Unit> get() = _authError

    fun likeById(id: Long) {
        if (!AppAuth.getInstance().state.value.isAuthorized) {
            _authError.value = Unit
            return
        }
        val likedByMe = data.value?.posts?.find { it.id == id }?.likedByMe ?: return
        viewModelScope.launch {
            try {
                repository.likeById(id, likedByMe)
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