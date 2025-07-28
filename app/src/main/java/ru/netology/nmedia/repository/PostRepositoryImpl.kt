package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)


    override fun get(): LiveData<List<Post>> {
        return dao.getAll().map { entities ->
            entities.map(PostEntity::toDto)
        }
    }

    override fun like(id: Long) {
        dao.likeById(id)
        data.value = posts
    }

    override fun repost(id: Long) {
        dao.repost(id)
        data.value = posts
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }
}