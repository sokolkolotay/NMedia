package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.TerminalSeparatorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedItem
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

    val data = repository.data
        .map { pagingData ->
            val now = System.currentTimeMillis() / 1_000
            pagingData
                .map<Post, FeedItem> { FeedItem.PostItem(it) }
                .insertSeparators(
                    terminalSeparatorType = TerminalSeparatorType.SOURCE_COMPLETE,
                ) { before, after ->
                    val afterPost = (after as? FeedItem.PostItem)?.post
                        ?: return@insertSeparators null
                    val afterPeriod = periodOf(afterPost, now)
                    val beforePeriod = (before as? FeedItem.PostItem)
                        ?.post
                        ?.let { periodOf(it, now) }

                    if (before == null || beforePeriod != afterPeriod) {
                        FeedItem.SeparatorItem(afterPeriod)
                    } else {
                        null
                    }
                }
        }
        .cachedIn(viewModelScope)
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

private fun periodOf(post: Post, now: Long): FeedItem.Period {
    val age = (now - post.published).coerceAtLeast(0)
    return when {
        age < 24 * 60 * 60 -> FeedItem.Period.TODAY
        age < 48 * 60 * 60 -> FeedItem.Period.YESTERDAY
        else -> FeedItem.Period.LAST_WEEK
    }
}
