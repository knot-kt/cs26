package com.knotkt.cs26.contracts

import kotlinx.serialization.SerialName
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

@Serializable
enum class ChatDeliveryStatus {
    ACCEPTED,
}

@Serializable
sealed class ChatStreamEvent {
    @Serializable
    @SerialName("message")
    data class Message(val message: ChatMessage) : ChatStreamEvent()

    @Serializable
    @SerialName("receipt")
    data class Receipt(
        val clientMessageId: String,
        val messageId: String,
        val status: ChatDeliveryStatus,
    ) : ChatStreamEvent()
}