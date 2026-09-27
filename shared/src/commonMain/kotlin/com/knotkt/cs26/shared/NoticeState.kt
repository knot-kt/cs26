package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.Notice

data class NoticeState(
    val notices: List<Notice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)