package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val clientMessageId: String,
    val content: String,
)

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val clientMessageId: String,
    val senderId: String,
    val content: String,
    val createdAtEpochMillis: Long,
)

@Serializable
data class MessagePage(
    val items: List<ChatMessage>,
)