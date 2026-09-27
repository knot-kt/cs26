package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.AuthSession

data class AuthState(
    val phone: String = "",
    val code: String = "",
    val expiresInSeconds: Int? = null,
    val developmentCode: String? = null,
    val session: AuthSession? = null,
    val isRequestingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val error: String? = null,
)
