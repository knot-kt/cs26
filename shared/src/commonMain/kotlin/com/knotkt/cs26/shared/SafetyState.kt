package com.knotkt.cs26.shared

data class SafetyState(
    val blockedUserIds: Set<String> = emptySet(),
    val isUpdating: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)