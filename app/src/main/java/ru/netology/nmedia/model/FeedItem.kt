package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

sealed interface FeedItem {
    data class PostItem(val post: Post) : FeedItem
    data class SeparatorItem(val period: Period) : FeedItem

    enum class Period {
        TODAY,
        YESTERDAY,
        LAST_WEEK,
    }
}
