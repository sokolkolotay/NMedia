package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = "",
    likes = 0,
    comments = 0,
    reposts = 0,
    views = 0,
    likeByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )
    val data = repository.get()
    val edited = MutableLiveData(empty)

    fun like(id: Long) = repository.like(id)
    fun repost(id: Long) = repository.repost(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun changeContent(content: String) {
        val text = content.trim()

        edited.value?.let {
            if (text == it.content) {
                return@let
            }

            edited.value = it.copy(content = text)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                repository.save(post)
            }
        }
        clearEdited()
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEdited() {
        edited.value = empty
        clearDraft()
    }

    //работа с черновиком
    private var draftMain: String? = null
    val draft: String?
        get() = draftMain

    fun saveDraft(text: String) {
        draftMain = text.trim()
    }

    fun clearDraft() {
        draftMain = null
    }

    fun startEditing(content: String?) {
        if (content != null) {
            edited.value = empty.copy(content = content)
        } else {
            // Новый пост — проверяем есть ли сохранённый черновик
            edited.value = empty.copy(content = draftMain ?: "")
        }
    }
}