package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.ChatMessage

data class ChatState(
    val conversationId: String = "demo",
    val input: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isConnecting: Boolean = false,
    val error: String? = null,
)