package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PostRepositorySharedPrefImpl(context: Context) : PostRepository {

    private val pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    private val key = POSTS_KEY
    private var posts = emptyList<Post>()
        set(value){
            field = value
            data.value = posts
            sync()
        }
    private val data = MutableLiveData(posts)
    private var nextId: Long = 1L

    init {
        pref.getString(POSTS_KEY, null)?.let {
            posts = gson.fromJson(it, type)
            nextId = (posts.maxOfOrNull { post -> post.id } ?: 0) + 1
        }
    }

    override fun get(): LiveData<List<Post>> = data
    override fun like(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    likeByMe = !post.likeByMe,
                    likes = if (post.likeByMe) post.likes - 1 else post.likes + 1
                )
            } else {
                post
            }
        }
    }

    override fun repost(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(reposts = post.reposts + 1)
            } else {
                post
            }
        }
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    published = "now"
                )
            ) + posts
        } else {
            posts.map {
                if (post.id == it.id) {
                    it.copy(content = post.content)
                } else it
            }
        }
    }

    private fun sync() {
        pref.edit().apply {
            putString(POSTS_KEY, gson.toJson(posts, type))
        }
    }

    companion object {
        private const val SHARED_PREF_NAME = "repo"
        private const val POSTS_KEY = "posts"
        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }
}
