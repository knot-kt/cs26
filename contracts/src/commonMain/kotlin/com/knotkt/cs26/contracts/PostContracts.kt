package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val content: String,
    val attachments: List<MediaAttachment> = emptyList(),
)

@Serializable
enum class MediaKind {
    IMAGE,
    AUDIO,
}

@Serializable
data class MediaAttachment(
    val kind: MediaKind,
    val objectKey: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMillis: Long? = null,
)

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val content: String,
    val createdAtEpochMillis: Long,
    val attachments: List<MediaAttachment> = emptyList(),
)

@Serializable
data class PostPage(
    val items: List<Post>,
)