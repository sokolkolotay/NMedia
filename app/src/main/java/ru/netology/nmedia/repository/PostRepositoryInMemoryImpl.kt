package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostRepositoryInMemoryImpl : PostRepository {

    private var posts = listOf(
        Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 999,
            reposts = 6_379
        ),
        Post(
            id = 2,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "18 сентября в 20:01",
            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
            likes = 999,
            reposts = 6_379
        ),
        Post(
            id = 3,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "01 января в 10:00",
            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
            likes = 100,
            reposts = 2_879
        ),
        Post(
            id = 4,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "23 декабря в 13:43",
            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
            likes = 1_020_000,
            reposts = 20_239
        )
    )

    private val data = MutableLiveData(posts)

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
        data.value = posts
    }

    override fun repost(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(reposts = post.reposts + 1)
            } else {
                post
            }
        }
        data.value = posts
    }
}