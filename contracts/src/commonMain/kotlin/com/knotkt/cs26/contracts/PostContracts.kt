package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val content: String,
    val attachments: List<MediaAttachment> = emptyList(),
    val type: PostType = PostType.GENERAL,
)

@Serializable
data class CreateCommentRequest(
    val content: String,
)

@Serializable
enum class MediaKind {
    IMAGE,
    AUDIO,
}

@Serializable
enum class PostType {
    GENERAL,
    RANT,
    HELP,
    EVENT,
    LOST_AND_FOUND,
    INTEREST,
    USED_ITEM,
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
data class MediaUploadResponse(
    val objectKey: String,
    val mimeType: String,
    val sizeBytes: Long,
)

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val content: String,
    val createdAtEpochMillis: Long,
    val attachments: List<MediaAttachment> = emptyList(),
    val type: PostType = PostType.GENERAL,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedByViewer: Boolean = false,
)

@Serializable
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val content: String,
    val createdAtEpochMillis: Long,
)

@Serializable
data class PostPage(
    val items: List<Post>,
    val nextCursor: String? = null,
)