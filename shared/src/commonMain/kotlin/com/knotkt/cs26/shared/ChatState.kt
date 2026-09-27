package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.ChatMessage
import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.SendMessageRequest

data class ChatState(
    val conversationId: String = "demo",
    val input: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val attachments: List<MediaAttachment> = emptyList(),
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val pendingMessages: List<SendMessageRequest> = emptyList(),
    val lastDeliveryStatus: String? = null,
    val isRecording: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
)