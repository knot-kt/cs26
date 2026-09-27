package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.ChatMessage
import com.knotkt.cs26.contracts.MediaAttachment

data class ChatState(
    val conversationId: String = "demo",
    val input: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val attachments: List<MediaAttachment> = emptyList(),
    val isConnecting: Boolean = false,
    val isRecording: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
)
