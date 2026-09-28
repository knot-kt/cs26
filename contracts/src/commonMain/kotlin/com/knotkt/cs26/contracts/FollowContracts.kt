package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class FollowStateResponse(
    val userId: String,
    val following: Boolean,
)

@Serializable
data class FollowingList(
    val userIds: List<String>,
)