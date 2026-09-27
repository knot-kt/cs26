package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class RequestCodeRequest(
    val phone: String,
)

@Serializable
data class RequestCodeResponse(
    val expiresInSeconds: Int,
    val developmentCode: String? = null,
)

@Serializable
data class VerifyCodeRequest(
    val phone: String,
    val code: String,
)

@Serializable
data class AuthSession(
    val userId: String,
    val accessToken: String,
)

@Serializable
data class AuthError(
    val code: String,
    val message: String,
)
