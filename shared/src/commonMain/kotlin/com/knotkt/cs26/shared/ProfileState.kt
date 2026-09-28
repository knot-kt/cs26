package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.Profile

data class ProfileState(
    val profile: Profile? = null,
    val nickname: String = "",
    val interestsInput: String = "",
    val anonymousByDefault: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)