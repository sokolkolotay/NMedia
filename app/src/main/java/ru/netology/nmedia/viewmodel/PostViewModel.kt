package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)


@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val dao: PostDao,
    private val appAuth: AppAuth
) : ViewModel() {


    val data: LiveData<FeedModel> =
        repository.data
            .map { posts ->
                FeedModel(
                    posts = posts,
                    empty = posts.isEmpty()
                )
            }
            .asLiveData()


    val edited = MutableLiveData(empty)


    private val _postCreated =
        SingleLiveEvent<Unit>()

    val postCreated: LiveData<Unit>
        get() = _postCreated


    private val _dataState =
        MutableLiveData<FeedModel>()

    val dataState: LiveData<FeedModel>
        get() = _dataState


    val newerCount =
        dao.getNewerCount()
            .asLiveData()



    private val _authError =
        SingleLiveEvent<Unit>()

    val authError: LiveData<Unit>
        get() = _authError



    init {
        loadPosts()
        startPollingNewer()
    }



    fun loadPosts() {

        viewModelScope.launch {

            _dataState.value =
                FeedModel(
                    loading = true
                )

            try {

                repository.getAll()

                _dataState.value =
                    FeedModel()

            } catch (e: Exception) {

                _dataState.value =
                    FeedModel(
                        error = true
                    )
            }
        }
    }



    private fun startPollingNewer() {

        viewModelScope.launch {

            delay(10_000)

            while (true) {

                val maxId =
                    dao.getMaxId() ?: 0L


                if (maxId > 0) {
                    repository.getNewer(maxId)
                }

                delay(10_000)
            }
        }
    }



    fun showNewPosts() {

        viewModelScope.launch {
            repository.showNewPosts()
        }
    }



    fun likeById(id: Long) {

        if (!appAuth.state.value.isAuthorized) {

            _authError.value = Unit
            return
        }


        val likedByMe =
            data.value
                ?.posts
                ?.find { it.id == id }
                ?.likedByMe
                ?: return


        viewModelScope.launch {

            try {

                repository.likeById(
                    id,
                    likedByMe
                )

            } catch (e: Exception) {

                _dataState.value =
                    FeedModel(
                        error = true
                    )
            }
        }
    }



    fun removeById(id: Long) {

        viewModelScope.launch {

            try {

                repository.removeById(id)

            } catch (e: Exception) {

                _dataState.value =
                    FeedModel(
                        error = true
                    )
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

                    _dataState.value =
                        FeedModel(
                            error = true
                        )
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

                _dataState.value =
                    FeedModel(
                        error = true
                    )
            }
        }
    }



    fun edit(post: Post) {
        edited.value = post
    }



    fun changeContent(content: String) {

        val text =
            content.trim()


        if (edited.value?.content == text)
            return


        edited.value =
            edited.value?.copy(
                content = text
            )
    }
}