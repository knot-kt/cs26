package com.knotkt.cs26.shared

data class FollowState(
    val followingIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
)