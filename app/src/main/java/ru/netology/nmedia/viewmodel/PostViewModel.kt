package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
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
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun likeById(id: Long) {
        val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return
        // Оптимистичное обновление: меняем локально сразу
        _data.value = _data.value?.copy(
            posts = _data.value?.posts.orEmpty().map { post ->
                if (post.id == id) post.copy(
                    likedByMe = !likedByMe,
                    likes = if (likedByMe) post.likes - 1 else post.likes + 1
                ) else post
            }
        )
        viewModelScope.launch {
            try {
                val updated = repository.likeById(id, likedByMe)
                // Обновляем актуальным ответом сервера
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().map {
                            if (it.id == id) updated else it
                        }
                    )
                )
            } catch (e: Exception) {
                // Откатываем оптимистичное изменение
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().map { post ->
                            if (post.id == id) post.copy(
                                likedByMe = likedByMe,
                                likes = if (likedByMe) post.likes + 1 else post.likes - 1
                            ) else post
                        },
                        error = true
                    )
                )
            }
        }
    }

    fun removeById(id: Long) {
        // Оптимистичное удаление
        val old = _data.value?.posts.orEmpty()
        _data.value = _data.value?.copy(
            posts = old.filter { it.id != id }
        )
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                // Откатываем
                _data.postValue(_data.value?.copy(posts = old, error = true))
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
                    _data.postValue(_data.value?.copy(error = true))
                }
            }
        }
        edited.value = empty
    }

    fun retrySave(post: Post) {
        viewModelScope.launch {
            try {
                repository.save(post)
                // После успеха перезагружаем список, чтобы убрать isSynced = false
                loadPosts()
            } catch (e: Exception) {
                _data.postValue(_data.value?.copy(error = true))
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