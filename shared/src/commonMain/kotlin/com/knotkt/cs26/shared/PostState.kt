package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.Post

data class PostState(
    val content: String = "",
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isPublishing: Boolean = false,
    val error: String? = null,
)