//package ru.netology.nmedia.repository
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import ru.netology.nmedia.dto.Post
//
//class PostRepositoryInMemoryImpl : PostRepository {
//    private var index: Long = 1L
//
//    private var posts = listOf(
//        Post(
//            id = index++,
//            author = "Нетология. Университет интернет-профессий будущего",
//            published = "21 мая в 18:36",
//            content = "В новом выпуске задаем 10 глупых вопросов разработчику в команде беспилотных автомобилей в Yandex.",
//            likes = 999,
//            reposts = 6_379,
//            video = "https://rutube.ru/video/feff0eff063797d5f0aa01e94ce3b59f/"
//        ),
//        Post(
//            id = index++,
//            author = "Нетология. Университет интернет-профессий будущего",
//            published = "21 мая в 18:36",
//            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
//            likes = 999,
//            reposts = 6_379,
//            video = null
//        ),
//        Post(
//            id = index++,
//            author = "Нетология. Университет интернет-профессий будущего",
//            published = "18 сентября в 20:01",
//            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
//            likes = 999,
//            reposts = 6_379,
//            video = null
//        ),
//        Post(
//            id = index++,
//            author = "Нетология. Университет интернет-профессий будущего",
//            published = "01 января в 10:00",
//            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
//            likes = 100,
//            reposts = 2_879,
//            video = null
//        ),
//        Post(
//            id = index++,
//            author = "Нетология. Университет интернет-профессий будущего",
//            published = "23 декабря в 13:43",
//            content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений",
//            likes = 1_020_000,
//            reposts = 20_239,
//            video = null
//        )
//    )
//
//    private val data = MutableLiveData(posts)
//
//    override fun get(): LiveData<List<Post>> = data
//
//    override fun like(id: Long) {
//        posts = posts.map { post ->
//            if (post.id == id) {
//                post.copy(
//                    likeByMe = !post.likeByMe,
//                    likes = if (post.likeByMe) post.likes - 1 else post.likes + 1
//                )
//            } else {
//                post
//            }
//        }
//        data.value = posts
//    }
//
//    override fun repost(id: Long) {
//        posts = posts.map { post ->
//            if (post.id == id) {
//                post.copy(reposts = post.reposts + 1)
//            } else {
//                post
//            }
//        }
//        data.value = posts
//    }
//
//    override fun removeById(id: Long) {
//        posts = posts.filter { it.id != id }
//        data.value = posts
//    }
//
//    override suspend fun save(post: Post) {
//        posts = if (post.id == 0L) {
//            listOf(
//                post.copy(
//                    id = index++,
//                    author = "Me",
//                    published = "now"
//                )
//            ) + posts
//        } else {
//            posts.map {
//                if (post.id == it.id) {
//                    it.copy(content = post.content)
//                } else it
//            }
//        }
//        data.value = posts
//    }
//}