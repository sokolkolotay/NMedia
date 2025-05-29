package ru.netology.nmedia

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

private fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> String.format("%.1fK", count / 1_000.0).replace(".0", "")
        count < 1_000_000 -> "${count / 1_000}K"
        else -> String.format("%.1fM", count / 1_000_000.0).replace(".0", "")
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb"
        )

        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = post.published
            countFavorite.text = formatCount(post.likes)
            countComment.text = formatCount(post.comments)
            countRepost.text = formatCount(post.reposts)
            countView.text = formatCount(post.views)

            if (post.likeByMe) {
                favorite.setImageResource(R.drawable.ic_fill_favorite_24dp)
            }

            favorite.setOnClickListener {
                post.likeByMe = !post.likeByMe

                if (post.likeByMe) {
                    post.likes++
                    favorite.setImageResource(R.drawable.ic_fill_favorite_24dp)
                } else {
                    post.likes--
                    favorite.setImageResource(R.drawable.ic_favorite_24dp)
                }
                countFavorite.text = formatCount(post.likes)
            }

            repost.setOnClickListener {
                post.reposts++
                countRepost.text = formatCount(post.reposts)
            }
        }
    }
}