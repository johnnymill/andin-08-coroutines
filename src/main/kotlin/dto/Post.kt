package ru.netology.coroutines.dto

data class Post(
    val id: Long,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    var attachment: Attachment? = null,
)

data class PostExtended(
    val post: Post,
    val author: Author? = null,
    val comments: List<Comment>? = null
)
