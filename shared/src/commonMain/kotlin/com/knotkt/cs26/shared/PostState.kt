package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.Comment
import com.knotkt.cs26.contracts.Post

data class PostState(
    val content: String = "",
    val posts: List<Post> = emptyList(),
    val commentsByPost: Map<String, List<Comment>> = emptyMap(),
    val commentInputs: Map<String, String> = emptyMap(),
    val attachments: List<MediaAttachment> = emptyList(),
    val isLoading: Boolean = false,
    val isPublishing: Boolean = false,
    val isUploading: Boolean = false,
    val isInteracting: Boolean = false,
    val error: String? = null,
)
