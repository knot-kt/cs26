package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val content: String,
)

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val content: String,
    val createdAtEpochMillis: Long,
)

@Serializable
data class PostPage(
    val items: List<Post>,
)