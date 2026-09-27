package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val clientMessageId: String,
    val content: String,
    val attachments: List<MediaAttachment> = emptyList(),
)

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val clientMessageId: String,
    val senderId: String,
    val content: String,
    val createdAtEpochMillis: Long,
    val attachments: List<MediaAttachment> = emptyList(),
)

@Serializable
data class MessagePage(
    val items: List<ChatMessage>,
)