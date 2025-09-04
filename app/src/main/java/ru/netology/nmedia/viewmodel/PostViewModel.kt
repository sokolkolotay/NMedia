package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }
    //старая реализация через getAll
//    fun loadPosts() {
//        thread {
//            // Начинаем загрузку
//            _data.postValue(FeedModel(loading = true))
//            try {
//                // Данные успешно получены
//                val posts = repository.getAll()
//                FeedModel(posts = posts, empty = posts.isEmpty())
//            } catch (e: IOException) {
//                // Получена ошибка
//                FeedModel(error = true)
//            }.also(_data::postValue)
//        }
//    }

    // реализация через getAllAsync
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

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }
    //старая реализация через likeById
//    fun likeById(id: Long) {
//        val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return
//        thread {
//            val newStatePost = repository.likeById(id, likedByMe)
//            _data.postValue(
//                _data.value?.copy(
//                    posts = _data.value?.posts.orEmpty().map {
//                        if (it.id == id) newStatePost
//                        else it
//                    }
//                )
//            )
//        }
//        edited.value = empty
//    }

    //новая реализация через LikeByIdAsync
    fun likeById(id: Long) {
        val likedByMe = _data.value?.posts?.find { it.id == id }?.likedByMe ?: return
        repository.likeByIdAsync(id, likedByMe, object : PostRepository.LikeByIdAsync {
            override fun onSuccess(post: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().map {
                            if (it.id == id) post else it
                        }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        })
    }

    fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(
                    posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

}
