package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val userId: String,
    val nickname: String,
    val interests: List<String> = emptyList(),
    val anonymousByDefault: Boolean = true,
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val interests: List<String> = emptyList(),
    val anonymousByDefault: Boolean = true,
)